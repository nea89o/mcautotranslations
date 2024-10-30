package moe.nea.mcautotranslations.gradle

import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.configurationcache.extensions.capitalized

abstract class MCAutoTranslationsExtension(private val project: Project) {
	abstract val translationFunction: Property<String>
	abstract val translationFunctionResolved: Property<String>

	fun collectTranslationsTaskFor(
		sourceSet: SourceSet,
		configure: CollectTranslations.() -> Unit = {}
	): CollectTranslations {
		val capName = if (sourceSet.name == "main") "" else sourceSet.name.capitalized()
		val taskName = "collect${capName}Translations"
		val task = project.tasks.findByName(taskName)?.let { it as CollectTranslations }
			?: project.tasks.create(taskName, CollectTranslations::class.java) {
				it.classes.from((sourceSet.extensions.findByName("kotlin") as SourceDirectorySet).classesDirectory)
			}
		configure(task)
		return task
	}
}
