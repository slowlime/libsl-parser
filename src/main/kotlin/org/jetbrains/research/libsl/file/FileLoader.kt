package org.jetbrains.research.libsl.file

interface FileLoader {
    fun load(path: String): LoadedFile
}

data class LoadedFile(
    val path: String,
    val canonicalPath: String,
    val contents: String,
)
