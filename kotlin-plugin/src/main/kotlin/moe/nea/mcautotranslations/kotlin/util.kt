package moe.nea.mcautotranslations.kotlin

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector


fun CompilerConfiguration.info(text: String) = messageCollector.info(text)
fun MessageCollector.info(text: String, place: CompilerMessageSourceLocation? = null) {
	report(CompilerMessageSeverity.WARNING, "MC Auto Translation: $text", place)
}
