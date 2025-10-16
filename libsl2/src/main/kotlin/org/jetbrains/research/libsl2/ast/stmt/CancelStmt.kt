package org.jetbrains.research.libsl2.ast.stmt

import org.jetbrains.research.libsl2.location.Location

data class CancelStmt(
    override var location: Location?,
) : Stmt
