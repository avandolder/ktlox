package com.craftinginterpreters.lox

fun evaluate(expr: Expr): Any? = when (expr) {
    is Expr.Literal -> expr.value
    is Expr.Grouping -> evaluate(expr.expression)
    is Expr.Unary -> {
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> -(right as Double)
            else -> null
        }
    }
    is Expr.Binary -> {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.BANG_EQUAL -> left != right
            TokenType.EQUAL_EQUAL -> left == right
            TokenType.GREATER -> left as Double > right as Double
            TokenType.GREATER_EQUAL -> left as Double >= right as Double
            TokenType.LESS -> (left as Double) < (right as Double)
            TokenType.LESS_EQUAL -> left as Double <= right as Double
            TokenType.MINUS -> left as Double - right as Double
            TokenType.PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> null
            }
            TokenType.SLASH -> left as Double / right as Double
            TokenType.STAR -> left as Double * right as Double
            else -> null
        }
    }
    is Expr.Ternary -> {
        val condition = evaluate(expr.condition)
        if (isTruthy(condition)) {
            evaluate(expr.left)
        } else {
            evaluate(expr.right)
        }
    }
}

fun isTruthy(obj: Any?) = when (obj) {
    null -> false
    is Boolean -> obj
    else -> true
}
