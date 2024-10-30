package moe.nea.mcautotranslations.gradle

import org.gradle.api.provider.Property

abstract class MCAutoTranslationsExtension {
	abstract val translationFunction: Property<String>
	abstract val translationFunctionResolved: Property<String>
}
