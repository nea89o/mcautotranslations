package moe.nea.mcautotranslations.example

data class Text(val key: String, val args: List<Any>)

fun trResolved(key: String, vararg args: Any) = Text(key, args.toList())
fun tr(key: String, default: String): Text = error("Did not run compiler plugin")
fun main() {
	println(tr("test1", "Hiiiiiii"))
	println(tr("test2", "Hello ${Math.random()}"))
	println(tr("test3", "Goodbye ${Math.random()} ${Math.E}"))
}
