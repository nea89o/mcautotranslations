package moe.nea.mcautotranslations.gradle

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import moe.nea.mcautotranslations.gradle.visitors.AnnotationCollector
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import java.io.File
import java.util.TreeMap

abstract class CollectTranslations : DefaultTask() {
	@get:InputFiles
	@get:Incremental
	abstract val baseTranslations: ConfigurableFileCollection

	@get:InputFiles
	@get:Incremental
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val classes: ConfigurableFileCollection

	@get:Internal
	abstract val cacheFile: RegularFileProperty

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	init {
		cacheFile.convention(project.layout.buildDirectory.file("mergeTranslations/incremental/${this.name}.json"))
		// TODO: should this second convention be changed?
		outputFile.convention(makeFileName("en_us.json"))
	}

	private fun makeFileName(name: String): Provider<RegularFile> {
		return project.layout.buildDirectory.file("mergeTranslations/build/${this.name}/$name")
	}

	fun outputFileName(name: String) {
		outputFile.set(makeFileName(name))
	}

	class Translations {
		var baseTranslation: TreeMap<String, TreeMap<String, String>> = TreeMap()
		var inlineTranslations: TreeMap<String, TreeMap<String, String>> = TreeMap()
	}

	companion object {
		val gson = Gson()
		val mapType: TypeToken<TreeMap<String, String>> = object : TypeToken<TreeMap<String, String>>() {}
	}

	@TaskAction
	fun execute(inputs: InputChanges) {
		val baseTranslationsDirty = inputs.getFileChanges(baseTranslations).any()
		val cacheFile = cacheFile.get().asFile
		cacheFile.parentFile.mkdirs()
		val cacheExists = cacheFile.exists()
		val canBeIncremental = cacheExists && !baseTranslationsDirty
		val baseTranslations: Translations = if (canBeIncremental) {
			gson.fromJson(cacheFile.readText(), Translations::class.java)
		} else {
			val t = Translations()
			baseTranslations.associateTo(t.baseTranslation) {
				it.toString() to gson.fromJson(it.readText(), mapType)
			}
			t
		}
		val files: List<FileChange> = if (canBeIncremental) {
			inputs.getFileChanges(classes).map { FileChange(it.file, it.normalizedPath) }
		} else {
			buildList {
				classes.asFileTree.visit {
					add(FileChange(it.file, it.path))
				}
			}
		}
		files
			.asSequence()
			.filter { checkFile(it.file) }
			.forEach {
				val className = getClassName(it.relativePath)
				if (it.file.exists()) {
					baseTranslations.inlineTranslations[className] = parseClassAnnotations(it.file)
				} else {
					baseTranslations.inlineTranslations.remove(className)
				}
			}
		cacheFile.writeText(gson.toJson(baseTranslations))
		outputFile.get().asFile.writeText(gson.toJson(toKVMap(baseTranslations)))
	}

	private fun toKVMap(translations: Translations): TreeMap<String, String> {
		return (translations.baseTranslation.values.asSequence()
			+ translations.inlineTranslations.values.asSequence())
			.fold(TreeMap()) { acc, x ->
				acc.putAll(x) // TODO: warn on duplicate properties (possibly with error enum configuration)
				acc
			}
	}

	private fun parseClassAnnotations(file: File): TreeMap<String, String> {
		val map = TreeMap<String, String>()
		kotlin.runCatching {
			ClassReader(file.readBytes())
				.accept(AnnotationCollector(map),
				        ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
		}.onFailure {
			throw RuntimeException("Could not parse annotations in $file", it)
		}
		return map
	}

	private fun getClassName(relativePath: String): String {
		return relativePath.replace("/", ".").removeSuffix(".class")
	}

	data class FileChange(val file: File, val relativePath: String)

	private fun checkFile(file: File): Boolean {
		if (file.isDirectory) return false
		val extension = file.extension
		if (extension == "kt" || extension == "java")
			error("Cannot collect translations from source files. Please attach the CollectTranslations task to a compile output")
		if (extension != "class") return false
		return true
	}
}
