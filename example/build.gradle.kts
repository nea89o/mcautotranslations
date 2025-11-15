plugins {
	kotlin("jvm") version "2.2.21"
	id("moe.nea.mc-auto-translations")
	application
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.google.code.gson:gson:2.11.0")
}

mcAutoTranslations {
	translationFunction.set("moe.nea.mcautotranslations.example.tr")
	translationFunctionResolved.set("moe.nea.mcautotranslations.example.trResolved")
}

mcAutoTranslations.collectTranslationsTaskFor(sourceSets.main.get()) {
	baseTranslations.from(file("en_us.json"))
	outputFileName("en_us.json")
}
tasks.processResources {
	from(mcAutoTranslations.collectTranslationsTaskFor(sourceSets.main.get())) {
		into("assets/minecraft/lang")
	}
}

application.mainClass.set("moe.nea.mcautotranslations.example.TestKt")
