package io.exercism.analyzer.kotlin.exercise.twofer

import com.google.gson.Gson
import kastree.ast.psi.Parser
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File


@RunWith(Parameterized::class)
class TwoFerTest(val path:String) {

    val twoFer = TwoFer("")
    val gson = Gson()

    companion object {
        const val basePath = "src/test/resources/"
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() : Collection<String> {

            return File(basePath)
                .listFiles()
                .filter { it.isDirectory }
                .map {it.name}
        }
    }

    @Test
    fun test() {
        val analysis: String = File("$basePath/$path/analysis.json").readText()
        twoFer.applyRules(Parser().parseFile(File("$basePath/$path/TwoFer.kt").readText()))
            .fold(
                { fail("Should not failed") },
                { assertEquals(analysis, gson.toJson(it)) }
            )
    }

}