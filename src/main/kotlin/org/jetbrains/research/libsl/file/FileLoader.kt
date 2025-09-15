package org.jetbrains.research.libsl.file

interface FileLoader {
    fun load(path: String): LoadedFile
}
