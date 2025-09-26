package org.jetbrains.research.libsl2.location

data class Location(val loadChain: LoadChain, val path: CanonicalPath, val line: Int, val column: Int) {
    fun pathLineColumn(): String = "$path:L$line:$column"

    fun lineColumn(): String = "$line:$column"

    override fun toString(): String {
        return pathLineColumn()
    }
}

fun Location?.inAt(): String = this?.run { "in $path at L$line:$column" } ?: "in <unknown>"
