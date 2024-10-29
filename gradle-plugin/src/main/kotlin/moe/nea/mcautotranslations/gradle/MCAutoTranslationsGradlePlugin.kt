package moe.nea.mcautotranslations.gradle

import moe.nea.mcautotranslation.`gradle-plugin`.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class MCAutoTranslationsGradlePlugin : KotlinCompilerPluginSupportPlugin {
	override fun apply(target: Project) {
		target.extensions.create("mcAutoTranslations", MCAutoTranslationsExtension::class.java)
	}

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		val project = kotlinCompilation.target.project
		val extension = project.extensions.getByType(MCAutoTranslationsExtension::class.java)
		return project.provider {
			listOf() // TODO: add plugin options from extension in here
		}
	}

	override fun getCompilerPluginId(): String {
		return BuildConfig.KOTLIN_PLUGIN_ID
	}

	override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
		groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
		artifactId = BuildConfig.KOTLIN_PLUGIN_ARTIFACT,
		version = BuildConfig.KOTLIN_PLUGIN_VERSION,
	)

	override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
		return true
	}
}