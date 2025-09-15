package org.jetbrains.research.libsl

import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.location.CanonicalPath
import org.jetbrains.research.libsl.file.FileLoader
import org.jetbrains.research.libsl.file.LoadedFile
import org.jetbrains.research.libsl.load.ModuleLoader
import org.jetbrains.research.libsl.location.LoadChain
import java.nio.file.Path

class LibSL(private val fileLoader: FileLoader) {
    internal sealed interface ModuleState {
        val file: LoadedFile

        data class InProgress(override val file: LoadedFile, val loadChain: LoadChain) : ModuleState
        data class Loaded(override val file: LoadedFile, val module: Module) : ModuleState
    }

    internal data class ModuleLoadRequest(internal var state: ModuleState)

    private val requestsByPath = mutableMapOf<CanonicalPath, ModuleLoadRequest>()
    private val requestQueue = ArrayDeque<ModuleLoadRequest>()

    fun load(path: Path): Module = load(path.toString())

    fun load(path: String): Module {
        val request = requestLoad(path, loadChain = null)
        processLoads()

        val state = request.state
        check(state is ModuleState.Loaded)

        return state.module
    }

    internal fun requestLoad(path: String, loadChain: LoadChain?): ModuleLoadRequest {
        val loadChain = loadChain ?: LoadChain.TopLevel(path)
        val file = fileLoader.load(path)

        return requestsByPath.getOrPut(file.canonicalPath) {
            ModuleLoadRequest(ModuleState.InProgress(file, loadChain))
                .also { requestQueue += it }
        }
    }

    private fun processLoads() {
        while (requestQueue.isNotEmpty()) {
            val request = requestQueue.removeFirst()
            val state = request.state
            check(state is ModuleState.InProgress)

            val module = ModuleLoader(this, state.file, state.loadChain).load()
            request.state = ModuleState.Loaded(state.file, module)
        }
    }
}
