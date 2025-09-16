package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.location.Location

data class Header(
    val location: Location,
    val libslVersion: String,
    val libraryName: String,
    val version: String?,
    val language: String?,
    val url: String?,
)
