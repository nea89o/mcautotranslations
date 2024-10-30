package moe.nea.mcautotranslations.gradle

import org.gradle.api.provider.Property

internal fun validateFunctionName(origin: String, property: Property<String>): Lazy<String> = lazy {
	property.finalizeValueOnRead()
	require(property.isPresent) {
		"Function property $origin has not been set yet."
	}
	val name = property.get()
	require(name.matches(Regex("^([a-z0-9_]+\\.)+[a-z0-9_A-Z]+$"))) {
		"'$name' is not a valid function name. Make sure to set $origin to a valid value."
	}
	name
}
