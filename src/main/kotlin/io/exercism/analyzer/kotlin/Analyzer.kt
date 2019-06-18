package io.exercism.analyzer.kotlin

import arrow.core.*
import arrow.instances.either.monad.flatMap
import io.exercism.analyzer.kotlin.exercise.Exercise
import io.exercism.analyzer.kotlin.exercise.NoSolutionError
import io.exercism.analyzer.kotlin.exercise.twofer.TwoFer

class Analyzer(val args: Array<String>) {


    fun run() {
        validateArgs()
            .flatMap { findSolution(it.a, it.b) }
            .flatMap {it.autoMentor()}
            .fold({ println("ERROR ${it.message}") }, { println("OK") })
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
    Analyzer(args).run()
}