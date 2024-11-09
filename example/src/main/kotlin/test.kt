package moe.nea.mcautotranslations.example

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val resources =
	Text::class.java.classLoader.getResourceAsStream("assets/minecraft/lang/en_us.json")!!.reader().use {
		Gson().fromJson(it, object : TypeToken<HashMap<String, String>>() {})
	}

class Text(val key: String, val args: Array<out Any>) {
	override fun toString(): String {
		return (resources[key] ?: error("Unresolved key $key")).format(*args)
	}
}

@Suppress("UNUSED")
fun trResolved(key: String, vararg args: Any) = Text(key, args)
@Suppress("UNUSED")
fun tr(key: String, default: String): Text = error("Did not run compiler plugin for key '$key' with default '$default'")
fun main() {
	println(tr("test1", "Hiiiiiii"))
	println(tr("test2", "Hello ${Math.random()}"))
	println(tr("test3", "Goodbye ${Math.random()} ${Math.E}"))
	println(OtherTest().testFunc(10, tr("lol", "Lolnea")))
	println(Test2.x())
}
