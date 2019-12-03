package com.craftinginterpreters.lox

class AstPrinter : Expr.Visitor<String> {
    fun print(expr: Expr) = expr.accept(this)

    override fun visitBinaryExpr(expr: Expr.Binary): String =
            parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitGroupingExpr(expr: Expr.Grouping): String =
            parenthesize("group", expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal): String =
            if (expr.value == null) "nil"
            else expr.value.toString()

    override fun visitUnaryExpr(expr: Expr.Unary): String =
            parenthesize(expr.operator.lexeme, expr.right)

    override fun visitTernaryExpr(expr: Expr.Ternary): String =
            parenthesize("ternary", expr.condition, expr.left, expr.right)

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append("($name")
        for (expr in exprs) {
            builder.append(" ${expr.accept(this)}")
        }
        builder.append(")")
        return builder.toString()
    }
}

fun convertToRpn(expr: Expr): String = when (expr) {
    is Expr.Binary -> "${convertToRpn(expr.left)} ${convertToRpn(expr.right)} ${expr.operator.lexeme}"
    is Expr.Grouping -> convertToRpn(expr.expression)
    is Expr.Literal -> if (expr.value == null) "null" else expr.value.toString()
    is Expr.Unary -> "${convertToRpn(expr.right)} ${expr.operator.lexeme}"
    is Expr.Ternary -> ""
}
