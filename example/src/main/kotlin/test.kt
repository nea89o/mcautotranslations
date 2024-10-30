package moe.nea.mcautotranslations.example

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val resources =
	Text::class.java.classLoader.getResourceAsStream("assets/minecraft/lang/en_us.json")!!.reader().use {
		Gson().fromJson(it, object : TypeToken<HashMap<String, String>>() {})
	}

class Text(val key: String, val args: Array<out Any>) {
	override fun toString(): String {
		return resources[key]!!.format(*args)
	}
}

fun trResolved(key: String, vararg args: Any) = Text(key, args)
fun tr(key: String, default: String): Text = error("Did not run compiler plugin")
fun main() {
	println(tr("test1", "Hiiiiiii"))
	println(tr("test2", "Hello ${Math.random()}"))
	println(tr("test3", "Goodbye ${Math.random()} ${Math.E}"))
}
