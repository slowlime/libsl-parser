package org.jetbrains.research.libsl2.ast

import org.antlr.v4.runtime.CharStreams
import org.jetbrains.research.libsl2.LibSLLexer
import org.jetbrains.research.libsl2.location.Location
import org.jetbrains.research.libsl2.location.LocationProvider

data class Name(
    override var location: Location?,
    var name: String,
) : LocationProvider {
    private fun needsEscaping(): Boolean {
        val charStream = CharStreams.fromString(name)
        val lexer = LibSLLexer(charStream).apply { removeErrorListeners() }
        val token = lexer.nextToken()

        return token == null || token.text != name
    }

    override fun toString(): String = if (needsEscaping()) {
        "`$name`"
    } else {
        name
    }
}
