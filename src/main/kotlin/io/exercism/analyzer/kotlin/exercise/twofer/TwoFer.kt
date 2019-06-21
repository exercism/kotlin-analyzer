package io.exercism.analyzer.kotlin.exercise.twofer

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.exercism.analyzer.kotlin.analysis.Analysis
import io.exercism.analyzer.kotlin.analysis.AnalysisStatus
import io.exercism.analyzer.kotlin.exercise.Exercise
import io.exercism.analyzer.kotlin.exercise.ExerciseError
import io.exercism.analyzer.kotlin.exercise.RuleError
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.psi.Parser

class TwoFer(override val path: String) : Exercise(path) {
    override fun applyRules(file: Node.File): Either<ExerciseError, Analysis> {
        var analysis: Analysis? = null
        var found: Boolean = false
        Visitor.visit(file) { v, _ ->
            when (v) {
                is Node.Decl.Func -> {
                    when {
                        v.name == "twofer" -> {
                            when {
                                found -> analysis = generateAnalysis(RuleError(ErrorComment.TOO_MANY_TWOFER_FUNCTION))
                                else -> {
                                    found = true
                                    //TODO: Replace with map or ValidatedNel
                                    //DISAPPROVE
                                    checkOnlyOneParameter(v)
                                        .flatMap { checkFirstParamNotNullable(it) }
                                        .flatMap { checkFirstParamHasDefaultValue(it, "you") }
                                        .flatMap { checkNoCondition(it) }
                                        .flatMap { checkUseLoop(it) }
                                        .flatMap { checkStringTemplate(it) }
                                        .flatMap { checkNoReturn(it) }
                                        //APPROVE
                                        .flatMap { checkInferenceType(it) }
                                        //APPROVE_OPTIMAL
                                        .flatMap { checkApproveOptimal(file, it) }
                                        .fold({
                                            analysis = generateAnalysis(it)
                                        }, {
                                            analysis = generateAnalysis()
                                        })
                                }
                            }
                        }
                    }

                }
            }
        }
        return when (analysis) {
            null -> generateAnalysis(RuleError(ErrorComment.NO_FUNCTION)).right()
            else -> analysis!!.right()
        }
    }

    private fun checkStringTemplate(func: Node.Decl.Func): Either<RuleError, Node.Decl.Func> {
        var useTokenAdd = false
        var useStringFormat = false
        Visitor.visit(func) { v, _ ->
            when (v) {
                is Node.Expr.Name -> {
                    with(v as Node.Expr.Name) {
                        when (this.name) {
                            "format" -> useStringFormat = true
                        }
                    }
                }
                is Node.Expr.BinaryOp.Oper.Token -> {
                    with(v as Node.Expr.BinaryOp.Oper.Token) {
                        when (this.token) {
                            Node.Expr.BinaryOp.Token.ADD -> useTokenAdd = true
                        }
                    }

                }
            }
        }
        return when {
            useTokenAdd || useStringFormat -> return RuleError(ErrorComment.NO_STRING_TEMPLATE).left()
            else -> return func.right()
        }
    }

    private fun checkNoReturn(func: Node.Decl.Func): Either<RuleError, Node.Decl.Func> {
        var useReturn = false
        Visitor.visit(func) { v, _ ->
            when (v) {
                is Node.Expr.Return -> useReturn = true
            }
        }
        return when {
            useReturn -> return RuleError(ErrorComment.NO_NEED_RETURN).left()
            else -> return func.right()
        }
    }

    private fun checkUseLoop(func: Node.Decl.Func): Either<RuleError, Node.Decl.Func> {
        var useLoop = false
        Visitor.visit(func) { v, _ ->
            when (v) {
                is Node.Expr.While, is Node.Expr.For -> useLoop = true
            }
        }
        return when {
            useLoop -> return RuleError(ErrorComment.USE_LOOP).left()
            else -> return func.right()
        }
    }

    private fun checkApproveOptimal(file: Node.File, func: Node.Decl.Func): Either<RuleError, Node.Decl.Func> {
        val variableName = func.params.first().name
        val optimalFile =
            Parser().parseFile("""fun twofer($variableName: String="you") = "One for ${'$'}$variableName, one for me."""")
        return when (file) {
            optimalFile -> func.right()
            else -> RuleError(ErrorComment.REFER_MENTOR).left()
        }
    }

    private fun checkNoCondition(func: Node.Decl.Func): Either<RuleError, Node.Decl.Func> {
        var useCondition = false
        Visitor.visit(func) { v, _ ->
            when (v) {
                is Node.Expr.If -> useCondition = true
            }
        }
        return when {
            useCondition -> return RuleError(ErrorComment.USE_CONDITION).left()
            else -> return func.right()
        }
    }

    private fun checkInferenceType(func: Node.Decl.Func): Either<RuleError, Node.Decl.Func> =
        when {
            func.type != null -> RuleError(ErrorComment.NO_INFERENCE_TYPE).left()
            else -> func.right()
        }


    private fun checkFirstParamHasDefaultValue(
        func: Node.Decl.Func,
        defaultValue: String
    ): Either<RuleError, Node.Decl.Func> =
        when (func.params.first().default) {
            is Node.Expr.StringTmpl ->
                with(func.params.first().default as Node.Expr.StringTmpl) {
                    when {
                        this.elems.size == 0 -> RuleError(ErrorComment.WRONG_DEFAULT_VALUE).left()
                        else -> when (this.elems.first()) {
                            is Node.Expr.StringTmpl.Elem.Regular ->
                                with(this.elems.first() as Node.Expr.StringTmpl.Elem.Regular) {
                                    when (defaultValue) {
                                        this.str -> func.right()
                                        else -> RuleError(ErrorComment.WRONG_DEFAULT_VALUE).left()
                                    }

                                }
                            else -> RuleError(ErrorComment.NO_DEFAULT_VALUE).left()
                        }
                    }
                }
            else -> RuleError(ErrorComment.NO_DEFAULT_VALUE).left()
        }

    private fun checkOnlyOneParameter(func: Node.Decl.Func): Either<RuleError, Node.Decl.Func> =
        when {
            func.params.count() == 1 -> func.right()
            else -> RuleError(ErrorComment.WRONG_PARAM).left()
        }


    private fun checkFirstParamNotNullable(func: Node.Decl.Func): Either<RuleError, Node.Decl.Func> =
        when {
            func.params.first().type?.ref is Node.TypeRef.Nullable -> RuleError(ErrorComment.PARAM_NULLABLE).left()
            else -> func.right()
        }


    private fun generateAnalysis(error: RuleError? = null): Analysis =
        when (error) {
            null -> Analysis(status = AnalysisStatus.approve_as_optimal, comments = ArrayList())
            else -> Analysis(
                status = error.comment.status,
                comments = listOf(error.comment.message ?: "")
            )
        }
}