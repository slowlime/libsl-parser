package org.jetbrains.research.libsl2.file

import java.nio.file.Path

interface FileLoader {
    /**
     * Loads a specification file by its path (local to the project).
     */
    fun load(path: String): LoadedFile

    /**
     * Loads a specification file by its path in the filesystem.
     */
    fun loadExternal(path: Path): LoadedFile
}
