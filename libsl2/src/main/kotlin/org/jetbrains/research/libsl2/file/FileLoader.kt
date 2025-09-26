package org.jetbrains.research.libsl2.file

interface FileLoader {
    fun load(path: String): LoadedFile
}
