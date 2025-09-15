package org.jetbrains.research.libsl.location

data class Location(val loadChain: LoadChain, val path: CanonicalPath, val line: Int, val column: Int)
