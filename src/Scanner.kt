package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

class Scanner(private val src: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var curr = 0
    private var line = 1

    private val keywords = mapOf(
            "and" to AND,
            "class" to CLASS,
            "else" to ELSE,
            "false" to FALSE,
            "for" to FOR,
            "fun" to FUN,
            "if" to IF,
            "nil" to NIL,
            "or" to OR,
            "print" to PRINT,
            "return" to RETURN,
            "super" to SUPER,
            "this" to THIS,
            "true" to TRUE,
            "var" to VAR,
            "while" to WHILE
    )

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = curr
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '?' -> addToken(QUESTION)
            ':' -> addToken(COLON)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> when {
                match('/') -> while (peek() != '\n' && !isAtEnd()) {
                    advance()
                }
                match('*') -> {
                    while (!isAtEnd() && !(match('*') && match('/'))) {
                        if (peek() == '\n') {
                            line++
                        }
                        advance()
                    }
                }
                else -> addToken(SLASH)
            }
            '\n' -> line++
            ' ', '\t', '\r' -> {
            }
            '"' -> string()
            else -> when {
                c.isDigit() -> number()
                isAlpha(c) -> identifier()
                else -> error(line, "Unexpected character.")
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        addToken(keywords[src.substring(start, curr)] ?: IDENTIFIER)
    }

    private fun number() {
        while (peek().isDigit()) {
            advance()
        }

        // Look for a fractional part.
        if (peek() == '.' && peekNext().isDigit()) {
            // Consume the "."
            advance()
            while (peek().isDigit()) {
                advance()
            }
        }

        addToken(NUMBER, src.substring(start, curr).toDouble())
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
            }
            advance()
        }

        // Unterminated string.
        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        // The closing ".
        advance()

        // Trim the surrounding quotes.
        addToken(STRING, src.substring(start + 1, curr - 1))
    }

    private fun match(expected: Char) =
            if (!isAtEnd() && src[curr] == expected) {
                curr++
                true
            } else {
                false
            }

    private fun peek() = if (isAtEnd()) '\u0000' else src[curr]

    private fun peekNext() = if (curr + 1 >= src.length) '\u0000' else src[curr + 1]

    private fun isAlpha(c: Char) = c.isLetter() || c == '_'

    private fun isAlphaNumeric(c: Char) = isAlpha(c) || c.isDigit()

    private fun isAtEnd() = curr >= src.length

    private fun advance() = src[curr++]

    private fun addToken(type: TokenType, literal: Any? = null) =
            tokens.add(Token(type, src.substring(start, curr), literal, line))
}
