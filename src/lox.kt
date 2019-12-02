package com.craftinginterpreters.lox

import java.io.File
import kotlin.system.exitProcess

var hadError = false

fun main(args: Array<String>) {
    when (args.size) {
        0 -> runPrompt()
        1 -> runFile(args[0])
        else -> {
            println("Usage: jlox [script]")
            exitProcess(64)
        }
    }
}

fun runFile(path: String) {
    run(File(path).readText(Charsets.UTF_8))

    if (hadError) {
        exitProcess(65)
    }
}

fun runPrompt() {
    while (true) {
        print("> ")
        run(readLine()!!)
    }
}

fun run(src: String) = Scanner(src).scanTokens().forEach { println(it) }

fun error(line: Int, msg: String) = report(line, "", msg)

fun report(line: Int, where: String, msg: String) {
    println("[line $line] Error$where: $msg")
    hadError = true
}