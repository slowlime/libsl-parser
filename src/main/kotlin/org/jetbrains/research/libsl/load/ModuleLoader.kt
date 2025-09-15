package org.jetbrains.research.libsl.load

import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.file.LoadedFile
import org.jetbrains.research.libsl.location.LoadChain

internal class ModuleLoader(private val libsl: LibSL, val file: LoadedFile, val loadChain: LoadChain) {
    fun load(): Module {
        TODO()
    }
}
