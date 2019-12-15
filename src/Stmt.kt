package com.craftinginterpreters.lox

sealed class Stmt {
    data class Block(val stmts: List<Stmt>) : Stmt()
    data class Expression(val expr: Expr) : Stmt()
    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt()
    data class Print(val expr: Expr) : Stmt()
    data class Var(val name: Token, val init: Expr?) : Stmt()
    data class While(val condition: Expr, val body: Stmt) : Stmt()
}
