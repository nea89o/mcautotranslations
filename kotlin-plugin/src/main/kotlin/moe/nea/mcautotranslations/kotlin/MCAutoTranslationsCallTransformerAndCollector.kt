package moe.nea.mcautotranslations.kotlin

import moe.nea.mcautotranslations.annotations.GatheredTranslation
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
class MCAutoTranslationsCallTransformerAndCollector(
	val file: IrFile,
	val irPluginContext: IrPluginContext,
	val messageCollector: MessageCollector,
	val translationNames: Map<FqName, CallableId>,
) : IrElementTransformerVoidWithContext() {

	override fun visitCall(expression: IrCall): IrExpression {
		val function = expression.symbol.owner
		val fqFunctionName = function.kotlinFqName
		val translatedFunctionName = translationNames[fqFunctionName]
			?: return super.visitCall(expression)
		if (expression.valueArgumentsCount != 2) {
			messageCollector.report(
				CompilerMessageSeverity.ERROR,
				"Translation calls need to have exactly two arguments. Use $fqFunctionName(\"translation.key\", \"some \$template string\")",
				expression.getCompilerMessageLocation(file)
			)
			return super.visitCall(expression)
		}
		val translationKey = expression.getValueArgument(0).asStringConst()
		if (translationKey == null) {
			messageCollector.report(
				CompilerMessageSeverity.ERROR,
				"The key of a translation call needs to be a constant string. Use $fqFunctionName(\"translation.key\", \"some \$template string\")",
				(expression.getValueArgument(0) ?: expression).getCompilerMessageLocation(file)
			)
			return super.visitCall(expression)
		}
		val translationDefault = expression.getValueArgument(1).asStringDyn()
		if (translationDefault == null) {
			messageCollector.report(
				CompilerMessageSeverity.ERROR,
				"The default of a translation call needs to be a string template or constant. Use $fqFunctionName(\"translation.key\", \"some \$template string\")",
				(expression.getValueArgument(1) ?: expression).getCompilerMessageLocation(file)
			)
			return super.visitCall(expression)

		}
		val symbol = currentScope!!.scope.scopeOwnerSymbol
		val builder = DeclarationIrBuilder(irPluginContext, symbol, expression.startOffset, expression.endOffset)
		return builder.generateTemplate(
			translatedFunctionName, translationKey,
			expression.getValueArgument(0)!!, translationDefault)
	}

	fun DeclarationIrBuilder.generateTemplate(
		translationName: CallableId,
		key: String,
		keySource: IrExpression,
		template: StringTemplate,
	): IrExpression {
		val replacementFunction = irPluginContext.referenceFunctions(translationName)
			.single() // TODO: find proper overload
		val arguments = splitTemplate(key, template)
		val varArgs = irVararg(context.irBuiltIns.anyType.makeNullable(), arguments)

		return irCall(
			replacementFunction,
			replacementFunction.owner.returnType,
		).apply {
			putValueArgument(0,
			                 constString(key, keySource.startOffset, keySource.endOffset))
			putValueArgument(1, varArgs)
		}
	}

	fun splitTemplate(key: String, template: StringTemplate): List<IrExpression> {
		var templateString = ""
		val arguments = mutableListOf<IrExpression>()
		for (segment in template.segments) {
			val const = segment.asStringConst()
			if (const != null) {
				templateString += const.replace("%", "%%")
			} else {
				templateString += "%s"
				arguments.add(segment)
			}
		}

		messageCollector.report(
			CompilerMessageSeverity.INFO,
			"Generated template: $templateString"
		)
		templates[key] = templateString
		return arguments
	}

	val templates: MutableMap<String, String> = mutableMapOf()


	fun finish() {
		val builder = DeclarationIrBuilder(irPluginContext, file.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
		val annotationCons = irPluginContext
			.referenceConstructors(GatheredTranslation::class.java.toClassId()).single()
		val annotations = templates.map {
			builder.irCallConstructor(annotationCons, listOf()).apply {
				putValueArgument(0, constString(it.key))
				putValueArgument(1, constString(it.value))
			}
		}
		val annotationContainer = file.declarations.singleOrNull()?.takeIf { it is IrClass } ?: file
		annotationContainer.annotations = annotations + annotationContainer.annotations
	}


	@Suppress("UNCHECKED_CAST")
	fun constString(
		text: String,
		startOffset: Int = SYNTHETIC_OFFSET,
		endOffset: Int = SYNTHETIC_OFFSET
	): IrConst =
		text.toIrConst(irPluginContext.irBuiltIns.stringType, startOffset, endOffset)
}

data class StringTemplate(
	val segments: List<IrExpression>,
) {
	constructor(vararg segments: IrExpression) : this(segments.toList())
}

fun IrExpression?.asStringDyn(): StringTemplate? = when (this) {
	is IrConst -> if (kind == IrConstKind.String) StringTemplate(this) else null
	is IrStringConcatenation -> StringTemplate(this.arguments)
	else -> null
}

fun IrExpression?.asStringConst(): String? = when (this) {
	is IrConst -> if (kind == IrConstKind.String) value as String else null
	is IrStringConcatenation -> this.arguments.singleOrNull().asStringConst()
	else -> null
}


fun Class<*>.toClassId(): ClassId {
	require(!this.isArray)
	require("." in this.name)
	return ClassId(
		FqName(this.name.substringBeforeLast(".")),
		Name.identifier(this.name.substringAfterLast(".").replace("$", ".")))
}
