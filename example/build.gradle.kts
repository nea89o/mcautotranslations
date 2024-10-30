import moe.nea.mcautotranslations.gradle.CollectTranslations

plugins {
	kotlin("jvm") version "2.0.20"
	id("moe.nea.mc-auto-translations")
}

repositories {
	mavenCentral()
}

tasks.register("collectTranslations", CollectTranslations::class) {
	this.baseTranslations.from(file("en_us.json"))
	this.classes.from(sourceSets.main.map { it.kotlin.classesDirectory })
	this.outputFile.set(layout.buildDirectory.file("compiled_en_us.json"))
}

