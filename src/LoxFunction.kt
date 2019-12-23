package com.craftinginterpreters.lox

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment) : LoxCallable {
    override fun arity() = declaration.params.size

    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        val env = Environment(closure)
        declaration.params.zip(args).forEach { (param, arg) -> env.define(param.lexeme, arg) }

        try {
            interpreter.executeBlock(declaration.body, env)
        } catch (returnValue: Return) {
            return returnValue.value
        }

        return null
    }

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}