package com.craftinginterpreters.lox

fun interpret(expr: Expr) = try {
    val value = evaluate(expr)
    println(stringify(value))
} catch (error: RuntimeError) {
    runtimeError(error)
}

fun evaluate(expr: Expr): Any? = when (expr) {
    is Expr.Literal -> expr.value
    is Expr.Grouping -> evaluate(expr.expression)
    is Expr.Unary -> {
        val right = evaluate(expr.right)
        when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }
            else -> null
        }
    }
    is Expr.Binary -> {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.BANG_EQUAL -> left != right
            TokenType.EQUAL_EQUAL -> left == right
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double > right as Double
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double >= right as Double
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < right as Double
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double <= right as Double
            }
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double - right as Double
            }
            TokenType.PLUS -> when {
                left is Double && right is Double -> left + right
                left is String -> left + stringify(right)
                right is String -> stringify(left) + right
                else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                if (right as Double != 0.0) left as Double / right
                else throw RuntimeError(expr.operator, "Division by Zero.")
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double * right as Double
            }
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

fun checkNumberOperand(operator: Token, operand: Any?): Unit = when (operand) {
    !is Double -> throw RuntimeError(operator, "Operand must be a number.")
    else -> {}
}

fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
    if (left !is Double || right !is Double) {
        throw RuntimeError(operator, "Operands must be numbers.")
    }
}

fun isTruthy(obj: Any?) = when (obj) {
    null -> false
    is Boolean -> obj
    else -> true
}

fun stringify(obj: Any?) = when (obj) {
    null -> "nil"
    is Double -> if (obj.toString().endsWith(".0")) {
        obj.toString().split('.')[0]
    } else {
        obj.toString()
    }
    else -> obj.toString()
}
