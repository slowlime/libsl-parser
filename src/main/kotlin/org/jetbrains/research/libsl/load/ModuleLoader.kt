package org.jetbrains.research.libsl.load

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.ast.BoolLit
import org.jetbrains.research.libsl.ast.CharLit
import org.jetbrains.research.libsl.ast.FloatLit
import org.jetbrains.research.libsl.ast.FullName
import org.jetbrains.research.libsl.ast.Generic
import org.jetbrains.research.libsl.ast.Header
import org.jetbrains.research.libsl.ast.IntLit
import org.jetbrains.research.libsl.ast.LibSLAnnotation
import org.jetbrains.research.libsl.ast.Module
import org.jetbrains.research.libsl.ast.Name
import org.jetbrains.research.libsl.ast.NullLit
import org.jetbrains.research.libsl.ast.PrimitiveLit
import org.jetbrains.research.libsl.ast.QualifiedTypeName
import org.jetbrains.research.libsl.ast.StringLit
import org.jetbrains.research.libsl.ast.TypeConstraint
import org.jetbrains.research.libsl.ast.Variance
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

    internal fun processAtomicExpr(ctx: LibSLParser.AtomicExprContext): Expr = ExprProcessor(this).run {
        when (ctx) {
            is LibSLParser.AtomicExprParenContext -> processAtomicExpr(ctx.inner)
            is LibSLParser.AtomicExprPrimitiveLitContext -> process(ctx)
            is LibSLParser.AtomicExprSignedNumLitContext -> process(ctx)
            is LibSLParser.AtomicExprArrayLitContext -> process(ctx.arrayLitExpr())
            is LibSLParser.AtomicExprAccessContext -> process(ctx)
            else -> error("unrecognized atomic expr $ctx")
        }
    }

    internal fun processExpr(ctx: LibSLParser.ExprContext): Expr = ExprProcessor(this).run {
        when (ctx) {
            is LibSLParser.ExprParenContext -> processExpr(ctx.inner)
            is LibSLParser.ExprPrimitiveLitContext -> process(ctx)
            is LibSLParser.ExprArrayLitContext -> process(ctx.arrayLitExpr())
            is LibSLParser.ExprPrevContext -> process(ctx)
            is LibSLParser.ExprProcCallContext -> process(ctx.procCallExpr())
            is LibSLParser.ExprActionCallContext -> process(ctx.actionCallExpr())
            is LibSLParser.ExprInstantiationContext -> process(ctx.instantiationExpr())
            is LibSLParser.ExprAccessContext -> process(ctx)
            is LibSLParser.ExprUnaryContext -> process(ctx)
            is LibSLParser.ExprHasConceptContext -> process(ctx)
            is LibSLParser.ExprTypeComparisonContext -> process(ctx)
            is LibSLParser.ExprCastContext -> process(ctx)
            is LibSLParser.ExprMultiplicativeContext -> process(ctx)
            is LibSLParser.ExprAdditiveContext -> process(ctx)
            is LibSLParser.ExprShiftContext -> process(ctx)
            is LibSLParser.ExprBitAndContext -> process(ctx)
            is LibSLParser.ExprBitXorContext -> process(ctx)
            is LibSLParser.ExprBitOrContext -> process(ctx)
            is LibSLParser.ExprRelationalContext -> process(ctx)
            is LibSLParser.ExprAndContext -> process(ctx)
            is LibSLParser.ExprOrContext -> process(ctx)
            else -> error("unrecognized expr $ctx")
        }
    }

    internal fun processAccess(ctx: LibSLParser.AccessContext): Access = AccessProcessor(this).run {
        when (ctx) {
            is LibSLParser.AccessNameContext -> process(ctx)
            is LibSLParser.AccessFieldContext -> process(ctx)
            is LibSLParser.AccessIndexContext -> process(ctx)
            else -> error("unrecognized access $ctx")
        }
    }

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

    private fun processGeneric(ctx: LibSLParser.GenericContext): Generic = Generic(
        ctx.variance?.let(::processVarianceSpec),
        processName(ctx.name),
    )

    private fun processVarianceSpec(ctx: LibSLParser.VarianceSpecContext): Variance = when (ctx) {
        is LibSLParser.CovariantContext -> Variance.Covariant
        is LibSLParser.ContravariantContext -> Variance.Contravariant
        is LibSLParser.InvariantContext -> Variance.Invariant
        else -> error("unrecognized variance spec $ctx")
    }

    internal fun processWhereClause(ctx: LibSLParser.WhereClauseContext): MutableList<TypeConstraint> =
        ctx.constraints.mapToMutable(::processTypeConstraint)

    private fun processTypeConstraint(ctx: LibSLParser.TypeConstraintContext): TypeConstraint = TypeConstraint(
        processName(ctx.param),
        ctx.variance?.let(::processVarianceSpec),
        processTypeArg(ctx.bound),
    )

    internal fun processSignedNumLit(ctx: LibSLParser.SignedNumLitContext): PrimitiveLit {
        val lit = when (ctx) {
            is LibSLParser.SignedNumLitIntContext -> processIntLit(processSign(ctx.sign()), ctx.IntegerLit().symbol)
            is LibSLParser.SignedNumLitFloatContext -> processFloatLit(processSign(ctx.sign()), ctx.FloatLit().symbol)
            else -> error("unrecognized signed num lit $ctx")
        }

        lit.location = locationOf(ctx)

        return lit
    }

    private fun processSign(ctx: LibSLParser.SignContext): Int = when (ctx) {
        is LibSLParser.PlusSignContext -> 1
        is LibSLParser.MinusSignContext -> -1
        else -> error("unrecognized sign $ctx")
    }

    internal fun processSignedIntLit(ctx: LibSLParser.SignedIntLitContext): IntLit = processIntLit(
        processSign(ctx.sign()),
        ctx.lit,
    )

    internal fun processPrimitiveLit(ctx: LibSLParser.PrimitiveLitContext): PrimitiveLit = when (ctx) {
        is LibSLParser.PrimitiveLitIntContext -> processIntLit(0, ctx.IntegerLit().symbol)
        is LibSLParser.PrimitiveLitFloatContext -> processFloatLit(0, ctx.FloatLit().symbol)
        is LibSLParser.PrimitiveLitStringLitContext -> processStringLit(ctx.StringLit().symbol)
        is LibSLParser.PrimitiveLitCharContext -> processCharLit(ctx.CharacterLit().symbol)
        is LibSLParser.PrimitiveLitTrueContext -> BoolLit(locationOf(ctx), true)
        is LibSLParser.PrimitiveLitFalseContext -> BoolLit(locationOf(ctx), false)
        is LibSLParser.PrimitiveLitNullContext -> NullLit(locationOf(ctx))
        else -> error("unrecognized primitive lit $ctx")
    }

    private fun String.splitSuffix(suffix: String, ignoreCase: Boolean = false): Pair<String, String>? =
        if (endsWith(suffix, ignoreCase)) {
            Pair(dropLast(suffix.length), suffix)
        } else {
            null
        }

    private fun String.splitPrefix(prefix: String, ignoreCase: Boolean = false): Pair<String, String>? =
        if (startsWith(prefix, ignoreCase)) {
            Pair(drop(prefix.length), prefix)
        } else {
            null
        }

    internal fun processIntLit(sign: Int, token: Token): IntLit {
        require(token.type == LibSLLexer.IntegerLit)

        var s = token.text

        val suffixSplit = s.splitSuffix("uL")
            ?: s.splitSuffix("l", ignoreCase = true)
            ?: s.splitSuffix("ux")
            ?: s.splitSuffix("x")
            ?: s.splitSuffix("us")
            ?: s.splitSuffix("s")
            ?: s.splitSuffix("u")
            ?: Pair(s, null)
        s = suffixSplit.first
        val suffix = suffixSplit.second

        val prefixSplit = s.splitPrefix("0x", ignoreCase = true)
            ?: s.splitPrefix("0b", ignoreCase = true)
            ?: s.splitPrefix("0")?.takeIf { it.first.isNotEmpty() }
            ?: Pair(s, null)
        s = prefixSplit.first

        val radix = when (prefixSplit.second) {
            "0x" -> 16
            "0b" -> 2
            "0" -> 8
            else -> 0
        }

        s = when {
            sign > 0 -> "+$s"
            sign < 0 -> "-$s"
            else -> s
        }

        val location = locationOf(token)

        return when (suffix) {
            "uL" -> IntLit.of(location, s.toULong(radix))
            "l", "L" -> IntLit.of(location, s.toLong(radix))
            "ux" -> IntLit.of(location, s.toUByte(radix))
            "x" -> IntLit.of(location, s.toByte(radix))
            "us" -> IntLit.of(location, s.toUShort(radix))
            "s" -> IntLit.of(location, s.toShort(radix))
            "u" -> IntLit.of(location, s.toUInt(radix))
            else -> IntLit.of(location, s.toInt(radix))
        }
    }

    internal fun processFloatLit(sign: Int, token: Token): FloatLit {
        require(token.type == LibSLLexer.FloatLit)

        var s = token.text

        val suffixSplit = s.splitSuffix("f", ignoreCase = true)
            ?: s.splitSuffix("d", ignoreCase = true)
            ?: Pair(s, "d")
        s = suffixSplit.first
        val suffix = suffixSplit.second

        s = when {
            sign > 0 -> "+$s"
            sign < 0 -> "-$s"
            else -> s
        }

        val location = locationOf(token)

        return when (suffix) {
            "f", "F" -> FloatLit.of(location, s.toFloat())
            else -> FloatLit.of(location, s.toDouble())
        }
    }

    private fun processStringLit(token: Token): StringLit {
        require(token.type == LibSLLexer.StringLit)

        return StringLit(
            locationOf(token),
            token.parseStringLit(),
        )
    }

    private fun processCharLit(token: Token): CharLit {
        require(token.type == LibSLLexer.CharacterLit)

        val s = token.text.substring(1, token.text.length - 1)

        return CharLit(
            locationOf(token),
            when (s) {
                "\\b" -> '\b'.code
                "\\t" -> '\t'.code
                "\\n" -> '\n'.code
                "\\f" -> 0x0c // form feed
                "\\r" -> '\r'.code
                "\\\"" -> '"'.code
                "\\'" -> '\''.code
                "\\\\" -> '\\'.code

                // unicode escape
                else if s.startsWith("\\u") -> s.substring(2).toInt(16)

                // octal escape
                else if s[0] in '0'..'7' -> s.substring(2).toInt(8)

                else if s.startsWith("\\") -> error("unrecognized escape sequence: '$s'")

                else -> s.codePointAt(0)
            },
        )
    }

    internal fun processTypeArg(ctx: LibSLParser.TypeArgContext): TypeArg = when (ctx) {
        is LibSLParser.TypeArgTypeExprContext -> processTypeExpr(ctx.typeExpr())
        is LibSLParser.TypeArgWildcardContext -> TypeArg.Wildcard(locationOf(ctx))
        else -> error("unrecognized type arg $ctx")
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
