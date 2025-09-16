package org.jetbrains.research.libsl.load

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.Header
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.decl.Decl
import org.jetbrains.research.libsl.file.LoadedFile
import org.jetbrains.research.libsl.location.LoadChain
import org.jetbrains.research.libsl.location.Location

internal class ModuleLoader(private val libsl: LibSL, val file: LoadedFile, val loadChain: LoadChain) {
    fun load(): Module {
        val stream = CharStreams.fromString(file.contents, file.canonicalPath.path)
        val lexer = LibSLLexer(stream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = LibSLParser(tokenStream)
        libsl.syntaxErrorListener?.let { parser.addErrorListener(it) }

        return processFile(parser.file())
    }

    internal fun locationOf(ctx: ParserRuleContext): Location = Location(
        loadChain,
        file.canonicalPath,
        line = ctx.start.line,
        column = ctx.start.charPositionInLine + 1,
    )

    private fun processFile(ctx: LibSLParser.FileContext): Module {
        val header = ctx.header()?.let(::processHeader)
        val decls = ctx.decls.map(::processDecl)

        return Module(locationOf(ctx), header, decls)
    }

    private fun processHeader(ctx: LibSLParser.HeaderContext): Header {
        TODO()
    }

    private fun processDecl(ctx: LibSLParser.GlobalDeclContext): Decl {
        return DeclProcessor(this).run {
            when (ctx) {
                is LibSLParser.GlobalDeclImportContext -> process(ctx.importDecl())
                else -> TODO()
            }
        }
    }
}
