package org.jetbrains.research.libsl2.ast.access

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.LocationProvider

sealed interface Access : LocationProvider

fun Access.walk(visitor: Visitor) {
    when (this) {
        is FieldAccess -> visitor.visit(this)
        is IndexAccess -> visitor.visit(this)
        is NameAccess -> visitor.visit(this)
        is AutomatonFieldAccess -> visitor.visit(this)
    }
}
