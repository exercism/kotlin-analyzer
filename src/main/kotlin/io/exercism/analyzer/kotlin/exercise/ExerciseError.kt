package io.exercism.analyzer.kotlin.exercise

import io.exercism.analyzer.kotlin.analysis.AnalysisStatus
import io.exercism.analyzer.kotlin.exercise.twofer.ErrorComment

sealed class ExerciseError() : Throwable()
class NoSolutionError : ExerciseError()
class PathError(override val message: String) : ExerciseError()
class SourceFileError(override val message: String) : ExerciseError()
class RuleError(val comment: ErrorComment) : ExerciseError()
class JsonGenerationError : ExerciseError()
