package org.jetbrains.research.libsl.ast.access

import org.jetbrains.research.libsl.ast.Visitor
import org.jetbrains.research.libsl.location.LocationProvider

sealed interface Access : LocationProvider

fun Access.walk(visitor: Visitor) {
    when (this) {
        is FieldAccess -> visitor.visit(this)
        is IndexAccess -> visitor.visit(this)
        is NameAccess -> visitor.visit(this)
    }
}
