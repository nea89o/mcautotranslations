plugins {
	kotlin("jvm")
	id("java-gradle-plugin")
}

dependencies {
	implementation(kotlin("gradle-plugin-api"))
	implementation(kotlin("stdlib"))
	implementation("com.google.code.gson:gson:2.11.0")
	implementation("org.ow2.asm:asm:9.7.1")
	implementation(project(":annotations"))
}

gradlePlugin {
	plugins {
		create("mcAutoTranslations") {
			id = "moe.nea.mc-auto-translations"
			displayName = "MC Auto Translation File Generation"
			implementationClass = "moe.nea.mcautotranslations.gradle.MCAutoTranslationsGradlePlugin"
		}
	}
}

// TODO: i shouldnt need this block, but let's leave it here if i need it in the future.
//publishing.publications {
//	create("maven", MavenPublication::class.java) {
//		from(components["java"])
//	}
//}
