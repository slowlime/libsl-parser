package org.jetbrains.research.libsl.ast.decl

import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.FunctionSignature
import org.jetbrains.research.libsl.location.Location

data class ShiftDecl(
    override val location: Location?,
    val from: MutableList<Name>,
    val to: Name,
    val by: MutableList<FunctionSignature>,
) : AutomatonMemberDecl
