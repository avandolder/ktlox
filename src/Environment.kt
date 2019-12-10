package com.craftinginterpreters.lox

class Environment(private val values: MutableMap<String, Any?> = mutableMapOf()) {
    fun get(name: Token): Any? = if (values.containsKey(name.lexeme)) {
        values[name.lexeme]
    } else {
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else {
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }
}