package org.jetbrains.research.libsl.location

@JvmInline
value class CanonicalPath(val path: String) {
    override fun toString(): String {
        return path
    }
}
