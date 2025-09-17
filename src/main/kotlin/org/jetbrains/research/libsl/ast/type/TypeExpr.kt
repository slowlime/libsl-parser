package org.jetbrains.research.libsl.ast.type

import org.jetbrains.research.libsl.location.LocationProvider

sealed interface TypeExpr : LocationProvider, TypeArg
