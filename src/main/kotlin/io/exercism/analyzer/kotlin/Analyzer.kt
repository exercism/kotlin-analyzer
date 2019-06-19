package io.exercism.analyzer.kotlin

import arrow.core.*
import arrow.instances.either.monad.flatMap
import io.exercism.analyzer.kotlin.exercise.Exercise
import io.exercism.analyzer.kotlin.exercise.NoSolutionError
import io.exercism.analyzer.kotlin.exercise.twofer.TwoFer
import mu.KotlinLogging

class Analyzer(private val args: Array<String>) {
    private val logger = KotlinLogging.logger {}


    fun run() {
        validateArgs()
            .flatMap { findSolution(it.a, it.b) }
            .flatMap { it.autoMentor() }
            .fold({ logger.error { it.message } }, { logger.info { "OK : $it" } })
    }

    private fun findSolution(slug: String, path: String): Either<NoSolutionError, Exercise> =
        when (slug) {
            "two-fer" -> TwoFer(path).right()
            else -> NoSolutionError().left()
        }

    private fun validateArgs(): Either<IllegalArgumentException, Tuple2<String, String>> {
        require(args.isNotEmpty()) { return IllegalArgumentException("No argument provided").left() }
        require(args.size == 2) { return IllegalArgumentException("Two arguments `slug` and`path` are not present").left() }
        return Tuple2(args[0], args[1]).right()
    }


}

fun main(args: Array<String>) {
    Try {
        System.setProperty("log.path", args[1])
    }.fold(
        { System.setProperty("log.path", "/opt/analyze") },
        { Unit })
    Analyzer(args).run()
}