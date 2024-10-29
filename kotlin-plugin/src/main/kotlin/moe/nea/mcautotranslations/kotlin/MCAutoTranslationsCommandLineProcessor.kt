@file:OptIn(ExperimentalCompilerApi::class)

package moe.nea.mcautotranslations.kotlin

import com.google.auto.service.AutoService
import moe.nea.mcautotranslation.`kotlin-plugin`.BuildConfig
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration


@AutoService(CommandLineProcessor::class)
class MCAutoTranslationsCommandLineProcessor : CommandLineProcessor {
	override val pluginId: String
		get() = BuildConfig.KOTLIN_PLUGIN_ID
	override val pluginOptions: Collection<AbstractCliOption>
		get() = listOf()

	override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
		// TODO: process options
	}
}