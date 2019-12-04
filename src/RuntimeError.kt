package com.craftinginterpreters.lox

class RuntimeError(val token: Token, val msg: String) : RuntimeException(msg)