package org.jetbrains.research.libsl.resolve

import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.ast.Module

internal class ModuleResolver(private val libsl: LibSL, private val module: Module) {
    fun resolve() {
        addTopLevelDefs()
        addImports()
        resolveTopLevelDefs()
    }

    private fun addTopLevelDefs() {
        TODO("Not yet implemented")
    }

    private fun addImports() {
        TODO("Not yet implemented")
    }

    private fun resolveTopLevelDefs() {
        TODO("Not yet implemented")
    }
}
