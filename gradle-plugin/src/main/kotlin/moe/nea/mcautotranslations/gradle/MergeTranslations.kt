package moe.nea.mcautotranslations.gradle

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import moe.nea.mcautotranslations.annotations.GatheredTranslation
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File

abstract class CollectTranslations : DefaultTask() {
	@get:InputFiles
	@get:Incremental
	abstract val baseTranslations: ConfigurableFileCollection

	@get:InputFiles
	@get:Incremental
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val classes: ConfigurableFileCollection

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	class Translations {
		var baseTranslation: HashMap<String, HashMap<String, String>> = HashMap()
		var inlineTranslations: HashMap<String, HashMap<String, String>> = HashMap()
	}

	companion object {
		val gson = Gson()
		val mapType: TypeToken<HashMap<String, String>> = object : TypeToken<HashMap<String, String>>() {}
	}

	@TaskAction
	fun execute(inputs: InputChanges) {
		val baseTranslationsDirty = inputs.getFileChanges(baseTranslations).any()
		val outFile = outputFile.get().asFile
		val outputExists = outFile.exists()
		val canBeIncremental = outputExists && !baseTranslationsDirty
		val baseTranslations: Translations = if (canBeIncremental) {
			gson.fromJson(outFile.readText(), Translations::class.java)
		} else {
			val t = Translations()
			baseTranslations.associateTo(t.baseTranslation) {
				it.toString() to gson.fromJson(outFile.readText(), mapType)
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
					parseClassAnnotations(it.file)
				} else {
					baseTranslations.inlineTranslations.remove(className)
				}
			}
		outFile.writeText(gson.toJson(baseTranslations))
	}


	private class KVVisitor(val map: MutableMap<String, String>) : AnnotationVisitor(Opcodes.ASM9) {
		var value: String? = null
		var key: String? = null
		override fun visit(name: String, value: Any) {
			when (name) {
				"key" -> this.key = value as String
				"value" -> this.value = value as String
				else -> error("Unknown annotation element $name")
			}
		}

		override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor {
			error("Unknown annotation element $name")
		}

		override fun visitArray(name: String?): AnnotationVisitor {
			error("Unknown annotation element $name")
		}

		override fun visitEnum(name: String?, descriptor: String?, value: String?) {
			error("Unknown annotation element $name")
		}

		override fun visitEnd() {
			map[key ?: error("Missing key")] = value ?: error("Missing value")
		}
	}

	private class RepeatableVisitor(val map: MutableMap<String, String>) : AnnotationVisitor(Opcodes.ASM9) {
		override fun visitArray(name: String?): AnnotationVisitor {
			require(name == "value") { "Unknown annotation element $name" }
			foundArray = true
			return KVVisitor(map)
		}

		var foundArray = false

		override fun visitEnd() {
			require(foundArray) { "Missing array" }
		}

		override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor {
			error("Unknown annotation element $name")
		}

		override fun visit(name: String?, value: Any?) {
			error("Unknown annotation element $name")
		}

		override fun visitEnum(name: String?, descriptor: String?, value: String?) {
			error("Unknown annotation element $name")
		}
	}

	private class AnnotationCollector(val map: MutableMap<String, String>) : ClassVisitor(Opcodes.ASM9) {
		override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
			// TODO: inject our own annotations into the classpath
			if (Type.getType(GatheredTranslation::class.java).descriptor.equals(descriptor)) {
				return KVVisitor(map)
			}
			if (Type.getType(GatheredTranslation.Repeatable::class.java).descriptor.equals(descriptor)) {
				return RepeatableVisitor(map)
			}
			// TODO: remove print log
			println("Ignoring descriptor $descriptor")
			return null
		}
	}

	private fun parseClassAnnotations(file: File): HashMap<String, String> {
		val map = HashMap<String, String>()
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