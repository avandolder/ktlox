package com.craftinginterpreters.lox

class Interpreter {
    val globals = Environment()
    private var env = globals

    init {
        globals.define("clock", object : LoxCallable {
            override fun arity() = 0
            override fun call(interpreter: Interpreter, args: List<Any?>): Any? = System.currentTimeMillis() / 1000.0
            override fun toString() = "<native fn>"
        })
    }

    fun interpret(stmts: List<Stmt>) = try {
        stmts.forEach(::execute)
    } catch (error: RuntimeError) {
        runtimeError(error)
    }

    private fun evaluate(expr: Expr): Any? = when (expr) {
        is Expr.Literal -> expr.value
        is Expr.Logical -> {
            val left = evaluate(expr.left)
            when {
                expr.operator.type == TokenType.OR && isTruthy(left) -> left
                !isTruthy(left) -> left
                else -> evaluate(expr.right)
            }
        }
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
        is Expr.Call -> {
            val callee = evaluate(expr.callee)
            val args = expr.args.map(::evaluate)
            if (callee is LoxCallable) {
                if (args.size != callee.arity()) {
                    throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${args.size}.")
                }
                callee.call(this, args)
            } else {
                throw RuntimeError(expr.paren, "Can only call functions and classes.")
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
        is Expr.Variable -> env.get(expr.name)
        is Expr.Assign -> {
            val value = evaluate(expr.value)
            env.assign(expr.name, value)
            value
        }
    }

    private fun execute(stmt: Stmt): Unit = when (stmt) {
        is Stmt.Block -> executeBlock(stmt.stmts, Environment(env))
        is Stmt.Expression -> {
            evaluate(stmt.expr)
            Unit
        }
        is Stmt.Function -> {
            val function = LoxFunction(stmt, env)
            env.define(stmt.name.lexeme, function)
        }
        is Stmt.If -> when {
            isTruthy(evaluate(stmt.condition)) -> execute(stmt.thenBranch)
            stmt.elseBranch != null -> execute(stmt.elseBranch)
            else -> {
            }
        }
        is Stmt.Print -> println(stringify(evaluate(stmt.expr)))
        is Stmt.Return -> throw Return(if (stmt.value != null) evaluate(stmt.value) else null)
        is Stmt.Var -> {
            val init = if (stmt.init != null) evaluate(stmt.init) else null
            env.define(stmt.name.lexeme, init)
        }
        is Stmt.While -> while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    fun executeBlock(stmts: List<Stmt>, env: Environment) {
        val prev = this.env
        try {
            this.env = env
            stmts.forEach(::execute)
        } finally {
            this.env = prev
        }
    }
}

fun checkNumberOperand(operator: Token, operand: Any?) {
    if (operand !is Double) {
        throw RuntimeError(operator, "Operand must be a number.")
    }
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
    is Expr.Literal -> obj.value.toString()
    else -> obj.toString()
}
