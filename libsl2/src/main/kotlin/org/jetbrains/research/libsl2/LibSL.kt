package org.jetbrains.research.libsl2

import org.antlr.v4.runtime.ANTLRErrorListener
import org.jetbrains.research.libsl2.ast.Module
import org.jetbrains.research.libsl2.exception.LibSLException
import org.jetbrains.research.libsl2.file.FileLoader
import org.jetbrains.research.libsl2.file.LoadedFile
import org.jetbrains.research.libsl2.load.ModuleLoader
import org.jetbrains.research.libsl2.location.CanonicalPath
import org.jetbrains.research.libsl2.location.LoadChain
import org.jetbrains.research.libsl2.resolve.NameResolver
import java.nio.file.Path

class LibSL(private val fileLoader: FileLoader) {
    sealed interface LoadResult {
        data class Ok(val module: Module) : LoadResult
        data class Error(val errors: List<LibSLException>) : LoadResult
    }

    var syntaxErrorListener: ANTLRErrorListener? = null

    internal sealed interface ModuleState {
        val file: LoadedFile

        data class InProgress(
            override val file: LoadedFile,
            val loadChain: LoadChain,
        ) : ModuleState {
            val onLoaded: MutableList<(Module) -> Unit> = mutableListOf()
        }

        data class Loaded(override val file: LoadedFile, val module: Module) : ModuleState
    }

    internal data class ModuleLoadRequest(var state: ModuleState)

    private val requestsByPath = mutableMapOf<CanonicalPath, ModuleLoadRequest>()
    private val requestQueue = ArrayDeque<ModuleLoadRequest>()

    fun load(path: String): LoadResult {
        val request = requestLoad(path, loadChain = null)

        return processLoadRequests(request)
    }

    fun loadExternal(path: Path): LoadResult {
        val file = fileLoader.loadExternal(path)
        val request = requestLoad(file, loadChain = null)

        return processLoadRequests(request)
    }

    fun loadFromString(path: String, canonicalPath: CanonicalPath, contents: String): LoadResult {
        val file = LoadedFile(path, canonicalPath, contents)
        val request = requestLoad(file, loadChain = null)

        return processLoadRequests(request)
    }

    private fun processLoadRequests(request: ModuleLoadRequest): LoadResult {
        val error = processLoadRequests()

        if (error != null) {
            return error
        }

        val state = request.state
        check(state is ModuleState.Loaded)

        return LoadResult.Ok(state.module)
    }

    internal fun requestLoad(path: String, loadChain: LoadChain?, onLoaded: (Module) -> Unit = {}): ModuleLoadRequest {
        val file = fileLoader.load(path)

        return requestLoad(file, loadChain, onLoaded)
    }

    private fun requestLoad(file: LoadedFile, loadChain: LoadChain?, onLoaded: (Module) -> Unit = {}): ModuleLoadRequest {
        val loadChain = loadChain ?: LoadChain.TopLevel(file.path)

        val request = requestsByPath.getOrPut(file.canonicalPath) {
            ModuleLoadRequest(ModuleState.InProgress(file, loadChain))
                .also { requestQueue += it }
        }

        when (val state = request.state) {
            is ModuleState.InProgress -> state.onLoaded += onLoaded
            is ModuleState.Loaded -> {
                onLoaded(state.module)
            }
        }

        return request
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
            state.onLoaded.forEach { it(module) }
        }

        return null
    }

    fun resolve(module: Module) {
        NameResolver(this, module).resolve()
    }
}
