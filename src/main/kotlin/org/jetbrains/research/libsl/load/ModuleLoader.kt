package org.jetbrains.research.libsl.load

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.Header
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.decl.GlobalDecl
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

    internal fun Token.parseStringLit(): String {
        require(type == LibSLLexer.StringLit)
        check(text.startsWith('"') && text.endsWith('"'))

        return text
            .substring(1, text.length - 1)
            .replace("\\\"", "'")
    }

    internal fun Token.parseIdent(): String {
        require(type == LibSLLexer.Identifier)

        return if (text.startsWith('`') && text.endsWith('`')) {
            text.substring(1, text.length - 1)
        } else {
            text
        }
    }

    private fun processFile(ctx: LibSLParser.FileContext): Module {
        val header = ctx.header()?.let(::processHeader)
        val decls = ctx.decls.flatMap(::processDecl)

        return Module(locationOf(ctx), header, decls)
    }

    private fun processHeader(ctx: LibSLParser.HeaderContext): Header {
        val libslVersion = ctx.libslVersion.parseStringLit()
        val libraryName = ctx.libraryName.parseIdent()
        val version = ctx.version?.parseStringLit()
        val language = ctx.language?.parseStringLit()
        val url = ctx.url?.parseStringLit()

        return Header(
            locationOf(ctx),
            libslVersion,
            libraryName,
            version,
            language,
            url,
        )
    }

    private fun processDecl(ctx: LibSLParser.GlobalDeclContext): Iterable<GlobalDecl> {
        return DeclProcessor(this).run {
            when (ctx) {
                is LibSLParser.GlobalDeclImportContext -> listOf(process(ctx.importDecl()))
                is LibSLParser.GlobalDeclIncludeContext -> listOf(process(ctx.includeDecl()))
                is LibSLParser.GlobalDeclSemanticTypeSectionContext -> ctx.semanticTypeSectionDecl().decls.map(this::process)
                is LibSLParser.GlobalDeclTypeAliasContext -> listOf(process(ctx.typeAliasDecl()))
                is LibSLParser.GlobalDeclStructContext -> listOf(process(ctx.structDecl()))
                is LibSLParser.GlobalDeclEnumContext -> listOf(process(ctx.enumDecl()))
                is LibSLParser.GlobalDeclAnnotationContext -> listOf(process(ctx.annotationDecl()))
                is LibSLParser.GlobalDeclActionContext -> listOf(process(ctx.actionDecl()))
                is LibSLParser.GlobalDeclAutomatonContext -> listOf(process(ctx.automatonDecl()))
                is LibSLParser.GlobalDeclFunctionContext -> listOf(process(ctx.functionDecl()))
                is LibSLParser.GlobalDeclVariableContext -> listOf(process(ctx.variableDecl()))
                else -> error("unknown AST node $ctx")
            }
        }
    }
}
