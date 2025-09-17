package org.jetbrains.research.libsl.load

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.ast.FunctionParam
import org.jetbrains.research.libsl.ast.Generic
import org.jetbrains.research.libsl.ast.Header
import org.jetbrains.research.libsl.ast.IntLit
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.PrimitiveLit
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.ast.contract.Contract
import org.jetbrains.research.libsl.ast.decl.GlobalDecl
import org.jetbrains.research.libsl.ast.decl.VariableDecl
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.stmt.Stmt
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.file.LoadedFile
import org.jetbrains.research.libsl.location.LoadChain
import org.jetbrains.research.libsl.location.Location

internal class ModuleLoader(val libsl: LibSL, val file: LoadedFile, val loadChain: LoadChain) {
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

    internal fun locationOf(token: Token): Location = Location(
        loadChain,
        file.canonicalPath,
        line = token.line,
        column = token.charPositionInLine + 1,
    )

    private fun processFile(ctx: LibSLParser.FileContext): Module {
        val header = ctx.header()?.let(::processHeader)
        val decls = ctx.decls.flatMap(::processGlobalDecl)

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

    private fun processGlobalDecl(ctx: LibSLParser.GlobalDeclContext): Iterable<GlobalDecl> = DeclProcessor(this).run {
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
            else -> error("unrecognized global decl $ctx")
        }
    }

    internal fun processVariableDecl(ctx: LibSLParser.VariableDeclContext): VariableDecl =
        DeclProcessor(this).process(ctx)

    internal fun processTypeExpr(ctx: LibSLParser.TypeExprContext): TypeExpr = TypeExprProcessor(this).run {
        when (ctx) {
            is LibSLParser.TypeExprPrimitiveLitContext -> process(ctx)
            is LibSLParser.TypeExprNameContext -> process(ctx.nameTypeExpr())
            is LibSLParser.TypeExprPointerContext -> process(ctx.pointerTypeExpr())
            is LibSLParser.TypeExprIntersectionContext -> process(ctx)
            is LibSLParser.TypeExprUnionContext -> process(ctx)
            else -> error("unrecognized type expr $ctx")
        }
    }

    internal fun processContract(ctx: LibSLParser.ContractContext): Contract = ContractProcessor(this).run {
        when (ctx) {
            is LibSLParser.ContractRequiresContext -> process(ctx.requiresContract())
            is LibSLParser.ContractEnsuresContext -> process(ctx.ensuresContract())
            is LibSLParser.ContractAssignsContext -> process(ctx.assignsContract())
            else -> error("unrecognized contract $ctx")
        }
    }

    internal fun processStmt(ctx: LibSLParser.StmtContext): Stmt = StmtProcessor(this).run {
        when (ctx) {
            is LibSLParser.StmtVariableDeclContext -> process(ctx)
            is LibSLParser.StmtIfContext -> process(ctx.ifStmt())
            is LibSLParser.StmtAssignContext -> process(ctx.assignStmt())
            is LibSLParser.StmtExprContext -> process(ctx)
            else -> error("unrecognized stmt $ctx")
        }
    }

    internal fun processAtomicExpr(ctx: LibSLParser.AtomicExprContext): Expr = TODO()

    internal fun processExpr(ctx: LibSLParser.ExprContext): Expr = TODO()

    internal fun processAccess(ctx: LibSLParser.AccessContext): Access = TODO()

    internal fun processPath(ctx: LibSLParser.PathContext): String {
        return when (ctx) {
            is LibSLParser.PathStringLitContext -> ctx.StringLit().symbol.parseStringLit()
            is LibSLParser.PathBareContext -> ctx.text
            else -> error("unrecognized path $ctx")
        }
    }

    internal fun processAnnotations(ctxs: List<LibSLParser.AnnotationContext>): MutableList<LibSLAnnotation> =
        ctxs.mapToMutable(::processAnnotation)

    private fun processAnnotation(ctx: LibSLParser.AnnotationContext): LibSLAnnotation = LibSLAnnotation(
        locationOf(ctx),
        processName(ctx.name),
        ctx.args.args.mapToMutable { arg ->
            LibSLAnnotation.Arg(
                arg.name?.let(::processName),
                processExpr(arg.value),
            )
        },
    )

    internal fun processName(name: TerminalNode): Name = processName(name.symbol)

    internal fun processName(name: Token): Name {
        require(name.type == LibSLLexer.Identifier)

        return Name(locationOf(name), name.parseIdent())
    }

    internal fun processQualifiedTypeName(ctx: LibSLParser.QualifiedTypeNameContext): QualifiedTypeName {
        val typeName = processFullName(ctx.typeName)
        val generics = ctx.typeParams?.let(::processGenerics) ?: mutableListOf()

        return QualifiedTypeName(typeName, generics)
    }

    internal fun processFullName(ctx: LibSLParser.FullNameContext): FullName = FullName(
        ctx.components.mapToMutable(::processName),
    )

    internal fun processGenerics(ctx: LibSLParser.GenericsContext): MutableList<Generic> =
        ctx.list?.params.mapToMutable(::processGeneric)

    private fun processGeneric(ctx: LibSLParser.GenericContext): Generic = TODO()

    internal fun processWhereClause(ctx: LibSLParser.WhereClauseContext): MutableList<TypeConstraint> = TODO()

    internal fun processSignedIntLit(ctx: LibSLParser.SignedIntLitContext): IntLit = TODO()

    internal fun processPrimitiveLit(ctx: LibSLParser.PrimitiveLitContext): PrimitiveLit = TODO()

    internal fun processTypeArg(ctx: LibSLParser.TypeArgContext): TypeArg = when (ctx) {
        is LibSLParser.TypeArgTypeExprContext -> processTypeExpr(ctx.typeExpr())
        is LibSLParser.TypeArgWildcardContext -> TypeArg.Wildcard(locationOf(ctx))
        else -> error("unknown type arg $ctx")
    }
}

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

internal fun <T> MutableList<T>?.orEmptyMutable(): MutableList<T> = this ?: mutableListOf()

internal fun <T, R> List<T>?.mapToMutable(transform: (T) -> R): MutableList<R> =
    this?.asSequence()?.map(transform)?.toMutableList().orEmptyMutable()

internal fun <T, R> List<T>?.flatMapToMutable(transform: (T) -> Iterable<R>): MutableList<R> =
    this?.asSequence()?.flatMap(transform)?.toMutableList().orEmptyMutable()
