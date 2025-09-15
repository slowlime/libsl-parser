package org.jetbrains.research.libsl.file

import org.jetbrains.research.libsl.location.CanonicalPath

data class LoadedFile(
    val path: String,
    val canonicalPath: CanonicalPath,
    val contents: String,
)

