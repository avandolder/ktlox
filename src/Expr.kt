package com.craftinginterpreters.lox

sealed class Expr {
    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
    data class Grouping(val expression: Expr) : Expr()
    data class Literal(val value: Any?) : Expr()
    data class Unary(val operator: Token, val right: Expr) : Expr()
    data class Ternary(val condition: Expr, val left: Expr, val right: Expr) : Expr()
    data class Variable(val name: Token) : Expr()
}
