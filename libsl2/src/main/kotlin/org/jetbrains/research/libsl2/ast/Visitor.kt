package org.jetbrains.research.libsl2.ast

import org.jetbrains.research.libsl2.ast.access.Access
import org.jetbrains.research.libsl2.ast.access.AutomatonFieldAccess
import org.jetbrains.research.libsl2.ast.access.FieldAccess
import org.jetbrains.research.libsl2.ast.access.IndexAccess
import org.jetbrains.research.libsl2.ast.access.NameAccess
import org.jetbrains.research.libsl2.ast.access.walk
import org.jetbrains.research.libsl2.ast.contract.AssignsContract
import org.jetbrains.research.libsl2.ast.contract.Contract
import org.jetbrains.research.libsl2.ast.contract.EnsuresContract
import org.jetbrains.research.libsl2.ast.contract.RequiresContract
import org.jetbrains.research.libsl2.ast.contract.walk
import org.jetbrains.research.libsl2.ast.decl.ActionDecl
import org.jetbrains.research.libsl2.ast.decl.AnnotationDecl
import org.jetbrains.research.libsl2.ast.decl.AutomatonDecl
import org.jetbrains.research.libsl2.ast.decl.ConstructorDecl
import org.jetbrains.research.libsl2.ast.decl.Decl
import org.jetbrains.research.libsl2.ast.decl.DestructorDecl
import org.jetbrains.research.libsl2.ast.decl.EnumDecl
import org.jetbrains.research.libsl2.ast.decl.FunctionDecl
import org.jetbrains.research.libsl2.ast.decl.ImportDecl
import org.jetbrains.research.libsl2.ast.decl.IncludeDecl
import org.jetbrains.research.libsl2.ast.decl.ProcDecl
import org.jetbrains.research.libsl2.ast.decl.SemanticTypeDecl
import org.jetbrains.research.libsl2.ast.decl.ShiftDecl
import org.jetbrains.research.libsl2.ast.decl.StateDecl
import org.jetbrains.research.libsl2.ast.decl.StructDecl
import org.jetbrains.research.libsl2.ast.decl.TypeAliasDecl
import org.jetbrains.research.libsl2.ast.decl.VariableDecl
import org.jetbrains.research.libsl2.ast.decl.walk
import org.jetbrains.research.libsl2.ast.expr.AccessExpr
import org.jetbrains.research.libsl2.ast.expr.ActionCallExpr
import org.jetbrains.research.libsl2.ast.expr.ArrayLitExpr
import org.jetbrains.research.libsl2.ast.expr.BinaryExpr
import org.jetbrains.research.libsl2.ast.expr.CastExpr
import org.jetbrains.research.libsl2.ast.expr.Expr
import org.jetbrains.research.libsl2.ast.expr.HasConceptExpr
import org.jetbrains.research.libsl2.ast.expr.InstantiationExpr
import org.jetbrains.research.libsl2.ast.expr.PrevExpr
import org.jetbrains.research.libsl2.ast.expr.PrimitiveLitExpr
import org.jetbrains.research.libsl2.ast.expr.ProcCallExpr
import org.jetbrains.research.libsl2.ast.expr.SetLitExpr
import org.jetbrains.research.libsl2.ast.expr.TypeCmpExpr
import org.jetbrains.research.libsl2.ast.expr.UnaryExpr
import org.jetbrains.research.libsl2.ast.expr.walk
import org.jetbrains.research.libsl2.ast.predicate.BlockPredicate
import org.jetbrains.research.libsl2.ast.predicate.ExprPredicate
import org.jetbrains.research.libsl2.ast.predicate.IfPredicate
import org.jetbrains.research.libsl2.ast.predicate.NamedPredicate
import org.jetbrains.research.libsl2.ast.predicate.Predicate
import org.jetbrains.research.libsl2.ast.predicate.VariableDeclPredicate
import org.jetbrains.research.libsl2.ast.predicate.walk
import org.jetbrains.research.libsl2.ast.stmt.AssignStmt
import org.jetbrains.research.libsl2.ast.stmt.CancelStmt
import org.jetbrains.research.libsl2.ast.stmt.ExprStmt
import org.jetbrains.research.libsl2.ast.stmt.IfStmt
import org.jetbrains.research.libsl2.ast.stmt.Stmt
import org.jetbrains.research.libsl2.ast.stmt.VariableDeclStmt
import org.jetbrains.research.libsl2.ast.stmt.walk
import org.jetbrains.research.libsl2.ast.type.IntersectionTypeExpr
import org.jetbrains.research.libsl2.ast.type.NameTypeExpr
import org.jetbrains.research.libsl2.ast.type.PointerTypeExpr
import org.jetbrains.research.libsl2.ast.type.PrimitiveLitTypeExpr
import org.jetbrains.research.libsl2.ast.type.TypeArg
import org.jetbrains.research.libsl2.ast.type.TypeExpr
import org.jetbrains.research.libsl2.ast.type.UnionTypeExpr
import org.jetbrains.research.libsl2.ast.type.walk

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

    open fun visit(predicate: Predicate) {
        predicate.walk(this)
    }

    open fun visit(predicate: BlockPredicate) {
        predicate.walk(this)
    }

    open fun visit(predicate: ExprPredicate) {
        predicate.walk(this)
    }

    open fun visit(predicate: IfPredicate) {
        predicate.walk(this)
    }

    open fun visit(predicate: NamedPredicate) {
        predicate.walk(this)
    }

    open fun visit(predicate: VariableDeclPredicate) {
        predicate.walk(this)
    }

    open fun visit(stmt: Stmt) {
        stmt.walk(this)
    }

    open fun visit(stmt: AssignStmt) {
        stmt.walk(this)
    }

    open fun visit(stmt: CancelStmt) {}

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

    open fun visit(expr: SetLitExpr) {
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

    open fun visit(access: AutomatonFieldAccess) {
        access.walk(this)
    }
}
