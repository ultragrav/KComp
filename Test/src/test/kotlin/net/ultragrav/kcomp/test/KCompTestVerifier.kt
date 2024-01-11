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

    @Test
    fun testAnnotated() {
        val expected = Pair(
            KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", KCompTest.testComponent)),
            KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", KCompTest.testComponent2))
        )
        val actual = KCompTest.annotated()

        assert(expected == actual) { "Expected $expected, got $actual" }
    }

    @Test
    fun testAnnotatedAlternative() {
        val expected = arrayOf(
            KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", KCompTest.testComponent)),
            KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", KCompTest.testComponent2))
        )
        val actual = KCompTest.annotatedAlternative()

        assert(expected.contentEquals(actual)) { "Expected ${expected.contentToString()}, got ${actual.contentToString()}" }
    }

    @Test
    fun testWithSpread() {
        val expected = arrayOf(
            KComp.miniMessage.deserialize("some string"),
            KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", KCompTest.testComponent)),
            KComp.miniMessage.deserialize("test <comp> test", Placeholder.component("comp", KCompTest.testComponent2))
        )
        val actual = KCompTest.withSpread()

        assert(expected.contentEquals(actual)) { "Expected ${expected.contentToString()}, got ${actual.contentToString()}" }
    }
}