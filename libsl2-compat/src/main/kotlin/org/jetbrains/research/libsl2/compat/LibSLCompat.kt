package org.jetbrains.research.libsl2.compat

import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.LslError
import org.jetbrains.research.libsl.nodes.Library
import org.jetbrains.research.libsl2.ast.Module
import org.jetbrains.research.libsl2.exception.LibSLException
import org.jetbrains.research.libsl2.file.FSFileLoader
import org.jetbrains.research.libsl2.location.CanonicalPath
import java.io.File
import java.nio.file.Path
import java.util.IdentityHashMap

@Suppress("unused", "CanBeParameter")
class LibSLCompat(
    private val basePath: String,
    val globalCtx: LslGlobalContext = LslGlobalContext(File(basePath).name),
) {
    val libsl = LibSL(basePath, globalCtx)

    private val fileLoader = FSFileLoader(Path.of(basePath))
    val libsl2 = org.jetbrains.research.libsl2.LibSL(fileLoader)

    private val translatedModules = IdentityHashMap<Module, Library>()

    fun loadFromFile(file: File): Library = handleLoadResult(file.path.toString(), libsl2.load(file.path))
    fun loadByPath(path: Path): Library = handleLoadResult(path.toString(), libsl2.load(path))
    fun loadFromPath(path: String): Library = handleLoadResult(path, libsl2.load(path))
    fun loadFromFileName(name: String): Library = loadFromPath(name)

    fun loadFromString(string: String, fileName: String): Library {
        val canonicalPath = CanonicalPath(Path.of(basePath).resolve(fileName).normalize().toString())

        return handleLoadResult(fileName, libsl2.loadFromString(fileName, canonicalPath, string))
    }

    internal fun addError(error: LibSLException): LslError {
        TODO()
    }

    internal fun addError(error: LslError): LslError = error.also { libsl.errorManager.addError(error) }

    private fun handleLoadResult(path: String, result: org.jetbrains.research.libsl2.LibSL.LoadResult): Library =
        when (result) {
            is org.jetbrains.research.libsl2.LibSL.LoadResult.Error -> {
                for (error in result.errors) {
                    addError(error)
                }

                Library(path, null)
            }

            is org.jetbrains.research.libsl2.LibSL.LoadResult.Ok -> {
                val rpo = result.module.getModuleGraphPostOrder().asReversed()
                val newlyTranslated = mutableListOf<ModuleTranslator.TranslatedLibrary>()

                for (module in rpo) {
                    translatedModules.getOrPut(module) {
                        ModuleTranslator(this, module).translate()
                            .also { newlyTranslated += it }
                            .library
                    }
                }

                for ((library, imports) in newlyTranslated) {
                    for ((path, module) in imports) {
                        library.importsMap[path] = translatedModules[module]!!
                    }
                }

                translatedModules[result.module]!!
            }
        }
}
