package com.craftinginterpreters.lox

fun printAst(expr: Expr): String = when (expr) {
    is Expr.Assign -> parenthesize("assign", Expr.Literal(expr.name.lexeme), expr.value)
    is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
    is Expr.Grouping -> parenthesize("group", expr.expression)
    is Expr.Literal -> if (expr.value == null) "nil" else expr.value.toString()
    is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
    is Expr.Ternary -> parenthesize("ternary", expr.condition, expr.left, expr.right)
    is Expr.Variable -> expr.name.lexeme
}

private fun parenthesize(name: String, vararg exprs: Expr): String =
        "($name ${exprs.map(::printAst).joinToString(" ")})"

fun printRpn(expr: Expr): String = when (expr) {
    is Expr.Binary -> "${printRpn(expr.left)} ${printRpn(expr.right)} ${expr.operator.lexeme}"
    is Expr.Grouping -> printRpn(expr.expression)
    is Expr.Literal -> if (expr.value == null) "null" else expr.value.toString()
    is Expr.Unary -> "${printRpn(expr.right)} ${expr.operator.lexeme}"
    else -> ""
}
