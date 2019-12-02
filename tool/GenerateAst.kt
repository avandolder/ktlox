package com.craftinginterpreters.lox

import java.io.File
import java.io.OutputStreamWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: generate_ast <output directory>")
        exitProcess(1)
    }

    val outputDir = args[0]
    defineAst(outputDir, "Expr", listOf(
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",                      
        "Literal  : Any? value",                         
        "Unary    : Token operator, Expr right"
    ))
}

fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = outputDir + "/" + baseName + ".kt"
    val writer = File(path).writer()
    writer.write("package com.craftinginterpreters.lox\n\n")

    writer.write("sealed class $baseName {\n")
    defineVisitor(writer, baseName, types)

    writer.write("    abstract fun <R> accept(visitor: Visitor<R>): R\n")

    // The AST classes.
    for (type in types) {
        val className = type.split(":")[0].trim()
        val fields = type.split(":")[1].trim()
        defineType(writer, baseName, className, fields)
    }

    writer.write("}\n")
    writer.close()
}

fun defineVisitor(writer: OutputStreamWriter, baseName: String, types: List<String>) {
    writer.write("    interface Visitor<R> {\n")
    for (type in types) {
        val typeName = type.split(":")[0].trim()
        writer.write("        fun visit$typeName$baseName(${baseName.toLowerCase()}: $typeName): R\n")
    }
    writer.write("    }\n\n")
}

fun defineType(
        writer: OutputStreamWriter, baseName: String,
        className: String, fields: String) {
    writer.write("    data class " + className + "(")
    val fieldItr = fields.split(", ").listIterator()
    while (fieldItr.hasNext()) {
        val (type, name) = fieldItr.next().split(" ")
        if (fieldItr.hasNext()) {
            writer.write("val $name: $type, ")
        } else {
            writer.write("val $name: $type")
        }
    }
    writer.write(") : $baseName() {\n")
    writer.write("        override fun <R> accept(visitor: Visitor<R>): R =\n")
    writer.write("                visitor.visit$className$baseName(this)\n")
    writer.write("    }\n")
}
