@file:OptIn(ExperimentalCompilerApi::class)

package moe.nea.mcautotranslations.kotlin

import com.google.auto.service.AutoService
import moe.nea.mcautotranslation.`kotlin-plugin`.BuildConfig
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.name.FqName


@AutoService(CommandLineProcessor::class)
class MCAutoTranslationsCommandLineProcessor : CommandLineProcessor {
	override val pluginId: String
		get() = BuildConfig.KOTLIN_PLUGIN_ID
	override val pluginOptions: Collection<AbstractCliOption>
		get() = listOf(resolvedFunctionOption, translateFunctionOption)

	companion object {
		val resolvedFunctionOption =
			CliOption(BuildConfig.PLUGIN_OPTION_RESOLVED_FUNCTION,
			          "fully qualified function name",
			          "Set the new replaced translate function.")
		val translateFunctionOption =
			CliOption(BuildConfig.PLUGIN_OPTION_TRANSLATE_FUNCTION,
			          "fully qualified function name",
			          "Set the original translate function that will be replaced with the a resolved function.")
		val translateFunction = CompilerConfigurationKey<FqName>(BuildConfig.PLUGIN_OPTION_TRANSLATE_FUNCTION)
		val resolvedFunction = CompilerConfigurationKey<FqName>(BuildConfig.PLUGIN_OPTION_RESOLVED_FUNCTION)
	}

	override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
		when (option.optionName) {
			resolvedFunctionOption.optionName ->
				configuration.put(translateFunction, FqName(value))

			translateFunctionOption.optionName ->
				configuration.put(resolvedFunction, FqName(value))
			else -> error("Unknown config option ${option.optionName}")
		}
	}
}