package moe.nea.mcautotranslations.gradle

import moe.nea.mcautotranslations.annotations.GatheredTranslation
import moe.nea.mcautotranslations.gradle.visitors.KVVisitor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class ArrayElementVisitor(val map: MutableMap<String, String>) : AnnotationVisitor(Opcodes.ASM9) {
	override fun visitArray(name: String?): AnnotationVisitor {
		error("Unknown annotation element $name")
	}

	override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor {
		require(descriptor == Type.getType(GatheredTranslation::class.java).descriptor) {
			"Invalid array element $name ($descriptor)"
		}
		return KVVisitor(map)
	}

	override fun visit(name: String?, value: Any?) {
		error("Unknown annotation element $name")
	}

	override fun visitEnum(name: String?, descriptor: String?, value: String?) {
		error("Unknown annotation element $name")
	}

	override fun visitEnd() {
		// no empty check
	}
}