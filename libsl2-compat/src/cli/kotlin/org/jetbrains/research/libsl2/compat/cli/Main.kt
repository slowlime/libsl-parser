package org.jetbrains.research.libsl2.compat.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import org.jetbrains.research.libsl2.compat.LibSLCompat
import java.nio.file.Path

private class LibSL2CompatCli : CliktCommand(name = "libsl2-compat-cli") {
    override fun run() = Unit
}

private class Dump : CliktCommand() {
    override fun help(context: Context): String = """
        Parse a LibSL file and dump it back as text.
    """.trimIndent()

    private val basePath by option("-b", "--base-path")
        .path(canBeFile = false)
        .defaultLazy { Path.of(".") }
        .help("The base path for resolving imports")

    private val file by argument().file()
        .help("The path to the LibSL file")

    override fun run() {
        val compat = LibSLCompat(basePath)
        val lib = compat.loadFromFile(file)

        for (error in compat.libsl.errorManager.errors) {
            echo(error.text, err = true)
        }

        echo(lib.dumpToString())
    }
}

fun main(args: Array<String>) = LibSL2CompatCli()
    .subcommands(Dump())
    .main(args)
