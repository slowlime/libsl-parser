package org.jetbrains.research.libsl.load

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.decl.ImportDecl

internal class DeclProcessor(private val loader: ModuleLoader) {
    fun process(ctx: LibSLParser.ImportDeclContext): ImportDecl = TODO()
}
