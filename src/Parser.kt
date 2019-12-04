package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.Expr.*
import com.craftinginterpreters.lox.TokenType.*
import java.lang.RuntimeException

class Parser(private val tokens: List<Token>) {
    private class ParseError : RuntimeException()

    private var curr = 0

    fun parse(): List<Stmt> {
        val stmts = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            stmts.add(statement())
        }
        return stmts
    }

    // expressionList -> expression ( "," expression )*
    private fun expressionList(): Expr {
        var expr = expression()

        while (match(COMMA)) {
            expr = expression()
        }

        return expr
    }

    private fun expression() = ternary()

    private fun statement() = if (match(PRINT)) printStatement() else expressionStatement()

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun ternary(): Expr {
        var expr = equality()

        if (match(QUESTION)) {
            val left = expression()
            match(COLON)
            val right = expression()
            expr = Ternary(expr, left, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = addition()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = addition()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun addition(): Expr {
        var expr = multiplication()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = multiplication()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun multiplication(): Expr {
        var expr = unary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Unary(operator, right)
        } else if (match(BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL,
                        LESS, LESS_EQUAL, MINUS, PLUS, SLASH, STAR)) {
            val operator = previous()
            // Parse and discard any following unary expression.
            unary()
            throw parseError(operator, "Unary '${operator.lexeme}' expressions are not supported.")
        }

        return primary()
    }

    private fun primary(): Expr = when {
        match(FALSE) -> Literal(false)
        match(TRUE) -> Literal(true)
        match(NIL) -> Literal(null)
        match(NUMBER, STRING) -> Literal(previous().literal)
        match(LEFT_PAREN) -> {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            Grouping(expr)
        }
        else -> throw parseError(peek(), "Expect expression.")
    }

    private fun match(vararg types: TokenType): Boolean {
        if (types.any(::check)) {
            advance()
            return true
        }

        return false
    }

    private fun consume(type: TokenType, msg: String) =
            if (check(type)) advance() else throw parseError(peek(), msg)

    private fun check(type: TokenType) = !isAtEnd() && peek().type == type

    private fun advance(): Token {
        if (!isAtEnd()) {
            curr++
        }
        return previous()
    }

    private fun isAtEnd() = peek().type == EOF
    private fun peek() = tokens[curr]
    private fun previous() = tokens[curr - 1]

    private fun parseError(token: Token, msg: String): ParseError {
        error(token, msg)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return
            }

            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }
}