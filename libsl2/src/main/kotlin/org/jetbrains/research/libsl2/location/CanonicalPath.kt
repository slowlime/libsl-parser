package org.jetbrains.research.libsl2.location

@JvmInline
value class CanonicalPath(val path: String) {
    override fun toString(): String {
        return path
    }
}
