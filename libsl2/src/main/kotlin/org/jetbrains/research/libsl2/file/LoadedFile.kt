package org.jetbrains.research.libsl2.file

import org.jetbrains.research.libsl2.location.CanonicalPath

data class LoadedFile(
    val path: String,
    val canonicalPath: CanonicalPath,
    val contents: String,
)

