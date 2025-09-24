package org.jetbrains.research.libsl.ast

import org.jetbrains.research.libsl.ast.access.Access
import org.jetbrains.research.libsl.ast.access.FieldAccess
import org.jetbrains.research.libsl.ast.access.IndexAccess
import org.jetbrains.research.libsl.ast.access.NameAccess
import org.jetbrains.research.libsl.ast.access.walk
import org.jetbrains.research.libsl.ast.contract.AssignsContract
import org.jetbrains.research.libsl.ast.contract.Contract
import org.jetbrains.research.libsl.ast.contract.EnsuresContract
import org.jetbrains.research.libsl.ast.contract.RequiresContract
import org.jetbrains.research.libsl.ast.contract.walk
import org.jetbrains.research.libsl.ast.decl.ActionDecl
import org.jetbrains.research.libsl.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl.ast.decl.ConstructorDecl
import org.jetbrains.research.libsl.ast.decl.Decl
import org.jetbrains.research.libsl.ast.decl.DestructorDecl
import org.jetbrains.research.libsl.ast.decl.EnumDecl
import org.jetbrains.research.libsl.ast.decl.FunctionDecl
import org.jetbrains.research.libsl.ast.decl.ImportDecl
import org.jetbrains.research.libsl.ast.decl.IncludeDecl
import org.jetbrains.research.libsl.ast.decl.ProcDecl
import org.jetbrains.research.libsl.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl.ast.decl.ShiftDecl
import org.jetbrains.research.libsl.ast.decl.StateDecl
import org.jetbrains.research.libsl.ast.decl.StructDecl
import org.jetbrains.research.libsl.ast.decl.TypeAliasDecl
import org.jetbrains.research.libsl.ast.decl.VariableDecl
import org.jetbrains.research.libsl.ast.decl.walk
import org.jetbrains.research.libsl.ast.expr.AccessExpr
import org.jetbrains.research.libsl.ast.expr.ActionCallExpr
import org.jetbrains.research.libsl.ast.expr.ArrayLitExpr
import org.jetbrains.research.libsl.ast.expr.BinaryExpr
import org.jetbrains.research.libsl.ast.expr.CastExpr
import org.jetbrains.research.libsl.ast.expr.Expr
import org.jetbrains.research.libsl.ast.expr.HasConceptExpr
import org.jetbrains.research.libsl.ast.expr.InstantiationExpr
import org.jetbrains.research.libsl.ast.expr.PrevExpr
import org.jetbrains.research.libsl.ast.expr.PrimitiveLitExpr
import org.jetbrains.research.libsl.ast.expr.ProcCallExpr
import org.jetbrains.research.libsl.ast.expr.TypeCmpExpr
import org.jetbrains.research.libsl.ast.expr.UnaryExpr
import org.jetbrains.research.libsl.ast.expr.walk
import org.jetbrains.research.libsl.ast.stmt.AssignStmt
import org.jetbrains.research.libsl.ast.stmt.ExprStmt
import org.jetbrains.research.libsl.ast.stmt.IfStmt
import org.jetbrains.research.libsl.ast.stmt.Stmt
import org.jetbrains.research.libsl.ast.stmt.VariableDeclStmt
import org.jetbrains.research.libsl.ast.stmt.walk
import org.jetbrains.research.libsl.ast.type.IntersectionTypeExpr
import org.jetbrains.research.libsl.ast.type.NameTypeExpr
import org.jetbrains.research.libsl.ast.type.PointerTypeExpr
import org.jetbrains.research.libsl.ast.type.PrimitiveLitTypeExpr
import org.jetbrains.research.libsl.ast.type.TypeArg
import org.jetbrains.research.libsl.ast.type.TypeExpr
import org.jetbrains.research.libsl.ast.type.UnionTypeExpr
import org.jetbrains.research.libsl.ast.type.walk

abstract class Visitor {
    open fun visit(module: Module) {
        module.walk(this)
    }

    fun visit(decl: Decl) {
        decl.walk(this)
    }

    open fun visit(decl: ActionDecl) {
        decl.walk(this)
    }

    open fun visit(decl: AnnotationDecl) {
        decl.walk(this)
    }

    open fun visit(decl: AutomatonDecl) {
        decl.walk(this)
    }

    open fun visit(decl: EnumDecl) {
        decl.walk(this)
    }

    open fun visit(decl: FunctionDecl) {
        decl.walk(this)
    }

    open fun visit(decl: ImportDecl) {}

    open fun visit(decl: IncludeDecl) {}

    open fun visit(decl: SemanticTypeDecl) {
        decl.walk(this)
    }

    open fun visit(decl: SemanticTypeDecl.Simple) {
        decl.walk(this)
    }

    open fun visit(decl: SemanticTypeDecl.Enumerated) {
        decl.walk(this)
    }

    open fun visit(decl: StructDecl) {
        decl.walk(this)
    }

    open fun visit(decl: TypeAliasDecl) {
        decl.walk(this)
    }

    open fun visit(decl: VariableDecl) {
        decl.walk(this)
    }

    open fun visit(decl: StateDecl) {}

    open fun visit(decl: ShiftDecl) {
        decl.walk(this)
    }

    open fun visit(decl: ConstructorDecl) {
        decl.walk(this)
    }

    open fun visit(decl: DestructorDecl) {
        decl.walk(this)
    }

    open fun visit(decl: ProcDecl) {
        decl.walk(this)
    }

    open fun visit(annotation: LibSLAnnotation) {
        annotation.walk(this)
    }

    open fun visit(typeExpr: TypeExpr) {
        typeExpr.walk(this)
    }

    open fun visit(typeExpr: IntersectionTypeExpr) {
        typeExpr.walk(this)
    }

    open fun visit(typeExpr: NameTypeExpr) {
        typeExpr.walk(this)
    }

    open fun visit(typeExpr: PointerTypeExpr) {
        typeExpr.walk(this)
    }

    open fun visit(typeExpr: PrimitiveLitTypeExpr) {}

    open fun visit(typeExpr: UnionTypeExpr) {
        typeExpr.walk(this)
    }

    open fun visit(typeArg: TypeArg) {
        typeArg.walk(this)
    }

    open fun visit(contract: Contract) {
        contract.walk(this)
    }

    open fun visit(contract: AssignsContract) {
        contract.walk(this)
    }

    open fun visit(contract: EnsuresContract) {
        contract.walk(this)
    }

    open fun visit(contract: RequiresContract) {
        contract.walk(this)
    }

    open fun visit(stmt: Stmt) {
        stmt.walk(this)
    }

    open fun visit(stmt: AssignStmt) {
        stmt.walk(this)
    }

    open fun visit(stmt: ExprStmt) {
        stmt.walk(this)
    }

    open fun visit(stmt: IfStmt) {
        stmt.walk(this)
    }

    open fun visit(stmt: VariableDeclStmt) {
        stmt.walk(this)
    }

    open fun visit(expr: Expr) {
        expr.walk(this)
    }

    open fun visit(expr: AccessExpr) {
        expr.walk(this)
    }

    open fun visit(expr: ActionCallExpr) {
        expr.walk(this)
    }

    open fun visit(expr: ArrayLitExpr) {
        expr.walk(this)
    }

    open fun visit(expr: BinaryExpr) {
        expr.walk(this)
    }

    open fun visit(expr: CastExpr) {
        expr.walk(this)
    }

    open fun visit(expr: HasConceptExpr) {
        expr.walk(this)
    }

    open fun visit(expr: InstantiationExpr) {
        expr.walk(this)
    }

    open fun visit(expr: PrevExpr) {
        expr.walk(this)
    }

    open fun visit(expr: PrimitiveLitExpr) {}

    open fun visit(expr: ProcCallExpr) {
        expr.walk(this)
    }

    open fun visit(expr: TypeCmpExpr) {
        expr.walk(this)
    }

    open fun visit(expr: UnaryExpr) {
        expr.walk(this)
    }

    open fun visit(access: Access) {
        access.walk(this)
    }

    open fun visit(access: FieldAccess) {
        access.walk(this)
    }

    open fun visit(access: IndexAccess) {
        access.walk(this)
    }

    open fun visit(access: NameAccess) {}
}
