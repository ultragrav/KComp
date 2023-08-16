import net.kyori.adventure.text.Component
import net.ultragrav.kcomp.KComp.toComp

fun main() {
    val comp = Component.text("test comp")
    val cond = comp == Component.text("test comp")
    val test = """
        Test multiline
          $comp
        more test
    """.trimIndent().toComp()

    val testList = listOf(
        "test1 $comp test2",
        "test3 $comp test4"
    ).toComp()

    println(("this is a test: $comp with more text" + "something" + if (cond) comp else "test").toComp())
    println(test)
    println(testList)
}
