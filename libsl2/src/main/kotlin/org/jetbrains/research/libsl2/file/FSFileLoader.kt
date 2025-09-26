package org.jetbrains.research.libsl2.file

import org.jetbrains.research.libsl2.location.CanonicalPath
import java.nio.file.Path

class FSFileLoader(private val basePath: Path) : FileLoader {
    private val cachedFiles = mutableMapOf<Path, String>()

    override fun load(path: String): LoadedFile {
        val canonicalPath = basePath.resolve(path).normalize()
        val contents = cachedFiles.getOrPut(canonicalPath) { canonicalPath.toFile().readText() }

        return LoadedFile(path, CanonicalPath(canonicalPath.toString()), contents)
    }
}
