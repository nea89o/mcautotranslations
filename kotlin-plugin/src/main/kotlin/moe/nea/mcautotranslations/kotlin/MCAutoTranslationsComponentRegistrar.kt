@file:OptIn(ExperimentalCompilerApi::class)

package moe.nea.mcautotranslations.kotlin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor
import kotlin.collections.getOrPut

@AutoService(CompilerPluginRegistrar::class)
class MCAutoTranslationsComponentRegistrar : CompilerPluginRegistrar() {
	override val supportsK2: Boolean
		get() = true

	override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
		val messageCollector = configuration.messageCollector
		IrGenerationExtension.registerExtension(
			extension = MCAutoTranslationsIrGenerationExtension(messageCollector))

		messageCollector.report(CompilerMessageSeverity.INFO, "Registering stuff")
	}

	fun <T : Any> ExtensionStorage.registerExtensionFirst(
		descriptor: ProjectExtensionDescriptor<T>,
		extension: T
	) {
		val extensions = (registeredExtensions as MutableMap<Any, Any>)
			.getOrPut(descriptor, { mutableListOf<Any>() }) as MutableList<T>
		extensions.add(0, extension)
	}

}