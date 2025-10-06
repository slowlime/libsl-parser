package org.jetbrains.research.libsl2.file

import org.jetbrains.research.libsl2.location.CanonicalPath
import java.nio.file.Path

class FSFileLoader(private val basePath: Path) : FileLoader {
    private val cachedFiles = mutableMapOf<Path, String>()

    private fun loadByCanonicalPath(canonicalPath: Path): String =
        cachedFiles.getOrPut(canonicalPath) { canonicalPath.toFile().readText() }

    override fun load(path: String): LoadedFile {
        val canonicalPath = basePath.resolve("$path.lsl").normalize()
        val contents = loadByCanonicalPath(canonicalPath)

        return LoadedFile(path, CanonicalPath(canonicalPath.toString()), contents)
    }

    override fun loadExternal(path: Path): LoadedFile {
        val canonicalPath = path.normalize()
        val contents = loadByCanonicalPath(canonicalPath)

        return LoadedFile(path.toString(), CanonicalPath(canonicalPath.toString()), contents)
    }
}
