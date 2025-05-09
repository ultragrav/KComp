package net.ultragrav.kcomp.test

import net.kyori.adventure.text.Component
import net.ultragrav.kcomp.toComp

object KCompTest {
    val testComponent = Component.text("test comp")
    val testComponent2 = Component.text("test comp2")

    fun withComponent(): Component {
        return "test $testComponent test".toComp()
    }

    fun withIf(): (Boolean) -> Component {
        return { "test ${if (it) testComponent else testComponent2 } test".toComp() }
    }

    fun withMultiline(): Component {
        return """
            Test multiline
              $testComponent
            more test
        """.trimIndent().toComp()
    }

    fun withList(): List<Component> {
        return listOf(
            "test1 $testComponent test2",
            "test3 $testComponent test4"
        ).toComp()
    }

    fun singleElementList(): List<Component> {
        return listOf("test1 $testComponent test2").toComp()
    }

    fun annotated(): Pair<Component, Component> {
        return testComp("test $testComponent test", "test $testComponent2 test")
    }

    fun annotatedAlternative(): Array<Component> {
        return testCompVararg("test $testComponent test", "test $testComponent2 test")
    }

    fun withSpread(): Array<Component> {
        val testArr = arrayOf("some string")

        return testCompVararg(*testArr, "test $testComponent test", "test $testComponent2 test")
    }
}