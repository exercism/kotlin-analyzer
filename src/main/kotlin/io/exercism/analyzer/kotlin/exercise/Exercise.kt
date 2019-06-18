package io.exercism.analyzer.kotlin.exercise


import arrow.core.*
import com.google.gson.Gson
import io.exercism.analyzer.kotlin.analysis.Analysis
import kastree.ast.Node
import kastree.ast.psi.Parser
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import java.nio.file.Paths

const val DIR_SRC_GRADLE = "/src/main/kotlin"

abstract class Exercise(open val path: String) {
    abstract val directoryName: String
    val gson = Gson()

    fun autoMentor(): Either<ExerciseError, Unit> {
        return checkPath()
            .flatMap { loadSourceFile() }
            .flatMap { extractCode(it) }
            .flatMap { applyRules(it) }
            .flatMap { generateJsonFile(it) }
            .fold({ it.left() }, { it.right() })
    }

    private fun generateJsonFile(analysis: Analysis): Either<JsonGenerationError, Unit> {
        return Try {
            File(path+"/analysis.json").writeText(gson.toJson(analysis))
        }.toEither { JsonGenerationError() }

    }

    abstract fun applyRules(file: Node.File): Either<ExerciseError, Analysis>


    private fun checkPath(): Either<PathError, Unit> =
        Try {
            require(path.isNotEmpty(), { "Path is not set" })
            with(Paths.get(path)) {
                require(Files.exists(this), { "Path doesn't exists" })
            }
            require(path.contains(this.directoryName), { "Path name should not match with Exercise" })
        }.toEither { PathError(it.message ?: "Error during checkPath") }

    private fun loadSourceFile(): Either<SourceFileError, String> =
        Try {
            File("$path$DIR_SRC_GRADLE")
                .listFiles(FileFilter { it.path.endsWith(".kt") })
                .map { it.absolutePath }
                .first()
        }.toEither { SourceFileError(it.message ?: "Error during loadSourceFile") }

    private fun extractCode(path: String): Either<SourceFileError, Node.File> =
        Try {
            with(
                Files.readAllLines(
                    Paths.get(path),
                    Charsets.UTF_8
                ).joinToString(separator = "")
            ) {
                Parser().parseFile(this)
            }
        }.toEither { SourceFileError("Source code not readable") }

}


