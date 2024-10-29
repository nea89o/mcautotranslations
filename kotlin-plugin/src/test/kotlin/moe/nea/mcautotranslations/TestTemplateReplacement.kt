@file:OptIn(ExperimentalCompilerApi::class)

package moe.nea.mcautotranslations

import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test

class TestTemplateReplacement {
	@Test
	fun testX() {
		val dollar = '$'
		compile(listOf(
			SourceFile.kotlin(
				"test.kt",
				"""
				package moe.nea.translatetest

				data class Text(val text: String)
				fun tr(key: String, value: String): Text {
					error("This should not be executed at runtime. Compiler plugin did not run.")
				}
				
				fun trResolved(key: String, vararg templateArgs: Any?): Text {
					return Text("TODO: do a lookup here for $dollar{key} and make use of $dollar{templateArgs.toList()}")
				}
				
				fun main() {
					val test = 20
					val othertest = "test2"

					println(tr("hi", "aaa$dollar{test}rest$dollar{othertest}end"))
					println(tr("hello", "just a regular strnig"))
					println(trResolved("hi", test, othertest))
				}
				""".trimIndent()
			)
		))
	}
}