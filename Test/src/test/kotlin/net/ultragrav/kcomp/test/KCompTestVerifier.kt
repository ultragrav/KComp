package net.ultragrav.kcomp.test

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.ultragrav.kcomp.KComp
import kotlin.test.Test

class KCompTestVerifier {
    @Test
    fun testWithComponent() {
        val testComponent = KCompTest.testComponent
        val expected = KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", testComponent))
        val actual = KCompTest.withComponent()

        assert(expected == actual) { "Expected $expected, got $actual" }
    }

    @Test
    fun testWithIf() {
        val testComponent = KCompTest.testComponent
        val testComponent2 = KCompTest.testComponent2

        val expectedTrue = KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", testComponent))
        val actualTrue = KCompTest.withIf()(true)

        val expectedFalse = KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", testComponent2))
        val actualFalse = KCompTest.withIf()(false)

        assert(expectedTrue == actualTrue) { "Expected $expectedTrue, got $actualTrue" }
        assert(expectedFalse == actualFalse) { "Expected $expectedFalse, got $actualFalse" }
    }

    @Test
    fun testWithMultiline() {
        val testComponent = KCompTest.testComponent
        val expected = KComp.miniMessage.deserialize("""
            Test multiline
              <comp>
            more test
        """.trimIndent(), Placeholder.component("comp", testComponent))
        val actual = KCompTest.withMultiline()

        assert(expected == actual) { "Expected $expected, got $actual" }
    }

    @Test
    fun testWithList() {
        val testComponent = KCompTest.testComponent
        val expected = listOf(
            KComp.miniMessage.deserialize("test1 <comp> test2", Placeholder.component("comp", testComponent)),
            KComp.miniMessage.deserialize("test3 <comp> test4", Placeholder.component("comp", testComponent))
        )
        val actual = KCompTest.withList()

        assert(expected == actual) { "Expected $expected, got $actual" }
    }
}