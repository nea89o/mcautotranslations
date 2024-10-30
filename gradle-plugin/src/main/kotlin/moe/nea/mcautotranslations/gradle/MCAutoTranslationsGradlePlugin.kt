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
		println("Applying plugin to project")
		target.extensions.create("mcAutoTranslations", MCAutoTranslationsExtension::class.java, target)
	}

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		val project = kotlinCompilation.target.project
		val extension = project.extensions.getByType(MCAutoTranslationsExtension::class.java)
		kotlinCompilation.dependencies {
			compileOnly(BuildConfig.ANNOTATIONS_GROUP + ":" + BuildConfig.ANNOTATIONS_ARTIFACT + ":" + BuildConfig.ANNOTATIONS_VERSION)
		}
		return project.provider {
			listOf(
				SubpluginOption(BuildConfig.PLUGIN_OPTION_RESOLVED_FUNCTION,
				                validateFunctionName("mcAutoTranslations.translationFunctionResolved",
				                                     extension.translationFunctionResolved)
				),
				SubpluginOption(BuildConfig.PLUGIN_OPTION_TRANSLATE_FUNCTION,
				                validateFunctionName("mcAutoTranslations.translationFunction",
				                                     extension.translationFunction)))
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
