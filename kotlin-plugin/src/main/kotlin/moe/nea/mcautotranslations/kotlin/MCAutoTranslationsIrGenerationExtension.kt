package moe.nea.mcautotranslations.kotlin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName

class MCAutoTranslationsIrGenerationExtension(
	private val messageCollector: MessageCollector,
	private val replace: FqName,
	private val resolved: FqName,
) : IrGenerationExtension {
	override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
		val translationNames: MutableMap<FqName, CallableId> = mutableMapOf()
		translationNames[replace] = CallableId(resolved.parent(), resolved.shortName())
		moduleFragment.files.forEach {
			val visitor = MCAutoTranslationsCallTransformerAndCollector(
				it,
				pluginContext,
				messageCollector,
				translationNames
			)
			it.accept(visitor, null)
			visitor.finish()
		}
	}
}
