package org.jetbrains.research.libsl2.compat

import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.LslError
import org.jetbrains.research.libsl.errors.ReferenceKind
import org.jetbrains.research.libsl.errors.UnresolvedReference
import org.jetbrains.research.libsl.nodes.Library
import org.jetbrains.research.libsl.utils.EntityPosition
import org.jetbrains.research.libsl.utils.PositionInfo
import org.jetbrains.research.libsl2.ast.Module
import org.jetbrains.research.libsl2.exception.LibSLException
import org.jetbrains.research.libsl2.exception.UnresolvedReferenceException
import org.jetbrains.research.libsl2.file.FSFileLoader
import org.jetbrains.research.libsl2.location.CanonicalPath
import org.jetbrains.research.libsl2.location.Location
import java.io.File
import java.nio.file.Path
import java.util.IdentityHashMap

@Suppress("unused")
class LibSLCompat(
    private val basePath: Path,
    val globalCtx: LslGlobalContext = LslGlobalContext(basePath.fileName.toString()),
) {
    val libsl = LibSL(basePath.toString(), globalCtx)

    private val fileLoader = FSFileLoader(basePath)
    val libsl2 = org.jetbrains.research.libsl2.LibSL(fileLoader)

    private val translatedModules = IdentityHashMap<Module, Library>()

    fun loadFromFile(file: File): Library = loadByPath(file.toPath())
    fun loadByPath(path: Path): Library = handleLoadResult(path.toString(), libsl2.loadExternal(path))
    fun loadFromPath(path: String): Library = loadByPath(Path.of(path))
    fun loadFromFileName(name: String): Library = handleLoadResult(name, libsl2.load(name))

    fun loadFromString(string: String, fileName: String): Library {
        val canonicalPath = CanonicalPath(basePath.resolve(fileName).normalize().toString())

        return handleLoadResult(fileName, libsl2.loadFromString(fileName, canonicalPath, string))
    }

    internal fun addError(exception: LibSLException): LslError {
        val error = when (exception) {
            is UnresolvedReferenceException -> UnresolvedReference(
                exception.fullMessage,
                exception.location!!.toEntityPosition(),
                when (exception.kind) {
                    UnresolvedReferenceException.Kind.Automaton -> ReferenceKind.Automaton
                    UnresolvedReferenceException.Kind.TypeParam -> ReferenceKind.Type
                    UnresolvedReferenceException.Kind.State -> ReferenceKind.State
                    UnresolvedReferenceException.Kind.Annotation -> ReferenceKind.Function
                    UnresolvedReferenceException.Kind.Param -> ReferenceKind.Variable
                    UnresolvedReferenceException.Kind.Type -> ReferenceKind.Type
                    UnresolvedReferenceException.Kind.Action -> ReferenceKind.Function
                    UnresolvedReferenceException.Kind.Binding -> ReferenceKind.Variable
                },
            )

            // no equivalent in libsl1: throw it immediately
            else -> throw exception
        }

        return addError(error)
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
                val postOrder = result.module.getModuleGraphPostOrder()
                val newlyTranslated = mutableListOf<ModuleTranslator.TranslatedLibrary>()

                for (module in postOrder) {
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

fun Location.toEntityPosition(): EntityPosition = EntityPosition(
    path.toString(),
    PositionInfo(line, column),
    // libsl2.ast.Location does not store the end position, since ANTLR does not provide a way to obtain it
    PositionInfo(line, column),
)
