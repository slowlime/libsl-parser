package org.jetbrains.research.libsl2.ast.predicate

import org.jetbrains.research.libsl2.location.Location

data class IfPredicate(
    override var location: Location?,
    var condition: Predicate,
    var thenBranch: Predicate,
    var elseBranch: Predicate?,
) : Predicate
