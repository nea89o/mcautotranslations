package moe.nea.mcautotranslations.gradle.visitors

import moe.nea.mcautotranslations.gradle.ArrayElementVisitor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes

internal class RepeatableVisitor(val map: MutableMap<String, String>) : AnnotationVisitor(Opcodes.ASM9) {
	override fun visitArray(name: String?): AnnotationVisitor {
		require(name == "value") { "Unknown annotation element $name" }
		foundArray = true
		return ArrayElementVisitor(map)
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