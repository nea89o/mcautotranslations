@file:OptIn(ExperimentalCompilerApi::class)

package moe.nea.mcautotranslations

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import moe.nea.mcautotranslations.kotlin.MCAutoTranslationsComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

private val DEFAULT_PLUGINS = arrayOf(
	MCAutoTranslationsComponentRegistrar()
)


fun compile(
	list: List<SourceFile>,
	vararg plugins: CompilerPluginRegistrar = DEFAULT_PLUGINS
): JvmCompilationResult {
	return KotlinCompilation().apply {
		sources = list
		compilerPluginRegistrars = plugins.toList()
		inheritClassPath = true
		messageOutputStream = System.out // TODO: capture this output somehow for testing
	}.compile()
}





