package moe.nea.mcautotranslations.gradle.visitors

import moe.nea.mcautotranslations.annotations.GatheredTranslation
import moe.nea.mcautotranslations.annotations.GatheredTranslations
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class AnnotationCollector(val map: MutableMap<String, String>) : ClassVisitor(Opcodes.ASM9) {
	override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
		if (Type.getType(GatheredTranslation::class.java).descriptor.equals(descriptor)) {
			return KVVisitor(map)
		}
		if (Type.getType(GatheredTranslations::class.java).descriptor.equals(descriptor)) {
			return RepeatableVisitor(map)
		}
		return null
	}
}