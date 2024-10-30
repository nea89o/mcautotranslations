# Auto Translation File Generator

This is a combination kotlin + gradle plugin that allows you to rewrite kotlin expressions like

```kt
source.sendFeedback(translate("some.translation.key", "Hallo, $name"))
```

into json based translation files:

```kt
source.sendFeedback(translateResolved("some.translation.key", name))
```

```json
{
	"some.translation.key": "Hallo, %s"
}
```

This is intended for minecraft mods, but is agnostic to anything that can work with a simple key-value json file using
`%s` string formatting.

> **Java function calls are not supported for now. If you think that should change, feel free to contribute a java
> compiler plugin (or pay me for implementing one, I don't really use Java myself)**

## Functions

First you need to create two top level functions in Kotlin:

```kt
@Suppress("UNUSED")
fun translateResolved(key: String, vararg args: Any): Text = TODO()
fun translate(key: String, default: String): Text = error("Did not run compiler plugin")
```

These two functions need to return the same type. The names of these function does not matter, but they need to not have
any overloads and must have distinct fully qualified names.

### Translate function

The `translate` function is the one you will be calling from your code.

The `key` argument needs to be a unique translation key `String` across all your files. It needs to be a string
*literal*, any dynamic code like variables, templates or methods will not work.

The `default` argument is used to generate the template. It can either be a string literal or a string template. If it
is a string template, each variable is supplied as an argument to the resolved function and replaced with a `%s` in the
generated translation file.

This function will never be called at runtime, so it should error out.

### Resolved function

The `translateResolved` function will be used by the generated code at runtime. It should read out the translation file
and use the provided translations to format a proper return value.

## Gradle setup

You need to add `maven("https://repo.nea.moe/releases")` as a repository to your `settings.gradle.kts`:


```kt
pluginManagement {
	repositories {
		maven {
			url = uri("https://repo.nea.moe/releases")
		}
		mavenCentral()
		gradlePluginPortal()
	}
}
```

Next you need to configure the gradle plugin:

```kt
plugins {
	kotlin("jvm") version "2.0.20"
	// Check https://repo.nea.moe/#/releases/moe/nea/mc-auto-translations/moe.nea.mc-auto-translations.gradle.plugin
	id("moe.nea.mc-auto-translations") version "0.0.1"
}

mcAutoTranslations {
	// Provide the fully qualified names of your translation functions here.
	translationFunction.set("moe.nea.mcautotranslations.example.tr")
	translationFunctionResolved.set("moe.nea.mcautotranslations.example.trResolved")
}

// mcAutoTranslations.collectTranslationsTaskFor is a convenience function.
// You can use the CollectTranslation class to create your own task that consumes translations from multiple source sets
// collectTranslationsTaskFor will only create a task if called and will always return the same task
mcAutoTranslations.collectTranslationsTaskFor(sourceSets.main.get()) {
	// If you have existing key value files you can use baseTranslations to load those
	baseTranslations.from(file("en_us.json"))
	// The output file name defaults to en_us.json. If you want another file name you can specify it like so
	// (or by directly setting the outputFile property)
	outputFileName("en_us.json")
}


tasks.processResources {
	// To actually inject the collected translations into your JAR, you can include it in processResources
	from(mcAutoTranslations.collectTranslationsTaskFor(sourceSets.main.get())) {
		into("assets/minecraft/lang")
	}
}
```
