package org.jetbrains.research.libsl

import org.antlr.v4.runtime.ANTLRErrorListener
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.exception.LibSLException
import org.jetbrains.research.libsl.location.CanonicalPath
import org.jetbrains.research.libsl.file.FileLoader
import org.jetbrains.research.libsl.file.LoadedFile
import org.jetbrains.research.libsl.load.ModuleLoader
import org.jetbrains.research.libsl.location.LoadChain
import java.nio.file.Path

class LibSL(private val fileLoader: FileLoader) {
    sealed interface LoadResult {
        data class Ok(val module: Module) : LoadResult
        data class Error(val errors: List<LibSLException>) : LoadResult
    }

    var syntaxErrorListener: ANTLRErrorListener? = null

    internal sealed interface ModuleState {
        val file: LoadedFile

        data class InProgress(override val file: LoadedFile, val loadChain: LoadChain) : ModuleState
        data class Loaded(override val file: LoadedFile, val module: Module) : ModuleState
    }

    internal data class ModuleLoadRequest(internal var state: ModuleState)

    private val requestsByPath = mutableMapOf<CanonicalPath, ModuleLoadRequest>()
    private val requestQueue = ArrayDeque<ModuleLoadRequest>()

    fun load(path: Path): LoadResult = load(path.toString())

    fun load(path: String): LoadResult {
        val request = requestLoad(path, loadChain = null)
        val error = processLoadRequests()

        if (error != null) {
            return error
        }

        val state = request.state
        check(state is ModuleState.Loaded)

        return LoadResult.Ok(state.module)
    }

    internal fun requestLoad(path: String, loadChain: LoadChain?): ModuleLoadRequest {
        val loadChain = loadChain ?: LoadChain.TopLevel(path)
        val file = fileLoader.load(path)

        return requestsByPath.getOrPut(file.canonicalPath) {
            ModuleLoadRequest(ModuleState.InProgress(file, loadChain))
                .also { requestQueue += it }
        }
    }

    private fun processLoadRequests(): LoadResult.Error? {
        while (requestQueue.isNotEmpty()) {
            val request = requestQueue.removeFirst()
            val state = request.state
            check(state is ModuleState.InProgress)

            val module = when (val result = ModuleLoader(this, state.file, state.loadChain).load()) {
                is LoadResult.Ok -> result.module
                is LoadResult.Error -> return result
            }

            request.state = ModuleState.Loaded(state.file, module)
        }

        return null
    }
}
