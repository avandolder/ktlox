package com.craftinginterpreters.lox

import java.io.File
import kotlin.system.exitProcess

var hadError = false
var hadRuntimeError = false

fun main(args: Array<String>) {
    when (args.size) {
        0 -> runPrompt()
        1 -> runFile(args[0])
        else -> {
            println("Usage: lox [script]")
            exitProcess(64)
        }
    }
}

fun runFile(path: String) {
    run(File(path).readText(Charsets.UTF_8))

    when {
        hadError -> exitProcess(65)
        hadRuntimeError -> exitProcess(70)
    }
}

fun runPrompt() {
    while (true) {
        print("> ")
        run(readLine()!!)
        hadError = false
    }
}

fun run(src: String) {
    val tokens = Scanner(src).scanTokens()
    val stmts = Parser(tokens).parse()

    if (hadError) {
        return
    }

    interpret(stmts)
}

fun error(line: Int, msg: String) = report(line, "", msg)

fun report(line: Int, where: String, msg: String) {
    println("[line $line] Error$where: $msg")
    hadError = true
}

fun error(token: Token, msg: String) = if (token.type == TokenType.EOF) {
    report(token.line, " at end", msg)
} else {
    report(token.line, " at '${token.lexeme}'", msg)
}

fun runtimeError(error: RuntimeError) {
    println("${error.msg}\n[line ${error.token.line}]")
    hadRuntimeError = true
}
