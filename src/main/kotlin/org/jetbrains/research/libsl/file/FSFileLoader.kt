package org.jetbrains.research.libsl.file

import java.nio.file.Path

class FSFileLoader(private val basePath: Path) : FileLoader {
    private val cachedFiles = HashMap<Path, String>()

    override fun load(path: String): LoadedFile {
        val canonicalPath = basePath.resolve(path).normalize()
        val contents = cachedFiles.getOrPut(canonicalPath) { canonicalPath.toFile().readText() }

        return LoadedFile(path, canonicalPath.toString(), contents)
    }
}
