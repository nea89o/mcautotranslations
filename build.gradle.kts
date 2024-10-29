import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "2.0.20" apply false
	id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
	id("com.gradle.plugin-publish") version "1.1.0" apply false
	id("com.github.gmazzo.buildconfig") version "5.5.0" apply false
}

allprojects {
	group = "moe.nea.mcautotranslations"
	version = "1.0-SNAPSHOT"

	repositories {
		mavenCentral()
	}
	tasks.withType<JavaCompile> {
		sourceCompatibility = "1.8"
		targetCompatibility = "1.8"
	}
	tasks.withType<KotlinCompile> {
		compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
	}
	tasks.withType<Test> {
		useJUnitPlatform()
	}
}
subprojects {
	apply(plugin = "com.github.gmazzo.buildconfig")
	configure<BuildConfigExtension> {
		packageName("moe.nea.mcautotranslation.${project.name}")
		buildConfigField<String>("KOTLIN_PLUGIN_ID", "moe.nea.mcautotranslations")
		buildConfigField<String>("KOTLIN_PLUGIN_GROUP", project(":kotlin-plugin").group.toString())
		buildConfigField<String>("KOTLIN_PLUGIN_ARTIFACT", project(":kotlin-plugin").name)
		buildConfigField<String>("KOTLIN_PLUGIN_VERSION", project(":kotlin-plugin").version.toString())
	}
}