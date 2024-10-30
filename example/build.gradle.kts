import moe.nea.mcautotranslations.gradle.CollectTranslations

plugins {
	kotlin("jvm") version "2.0.20"
	id("moe.nea.mc-auto-translations")
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

val collectTranslations by tasks.registering(CollectTranslations::class) {
	this.baseTranslations.from(file("en_us.json"))
	this.classes.from(sourceSets.main.map { it.kotlin.classesDirectory })
	this.outputFile.set(layout.buildDirectory.file("en_us.json"))
}

tasks.processResources { from(collectTranslations) }
