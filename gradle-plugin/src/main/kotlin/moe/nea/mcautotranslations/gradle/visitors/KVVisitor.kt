package moe.nea.mcautotranslations.gradle.visitors

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes

internal class KVVisitor(val map: MutableMap<String, String>) : AnnotationVisitor(Opcodes.ASM9) {
	var value: String? = null
	var key: String? = null
	override fun visit(name: String, value: Any) {
		when (name) {
			"translationKey" -> this.key = value as String
			"translationValue" -> this.value = value as String
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