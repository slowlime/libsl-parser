package org.jetbrains.research.libsl2.ast.contract

import org.jetbrains.research.libsl2.ast.Visitor
import org.jetbrains.research.libsl2.location.LocationProvider

sealed interface Contract : LocationProvider

fun Contract.walk(visitor: Visitor) {
    when (this) {
        is AssignsContract -> visitor.visit(this)
        is EnsuresContract -> visitor.visit(this)
        is RequiresContract -> visitor.visit(this)
    }
}
