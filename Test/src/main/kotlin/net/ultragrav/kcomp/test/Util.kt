package net.ultragrav.kcomp.test

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.ultragrav.kcomp.ComponentPlaceholderInserting
import net.ultragrav.kcomp.toComp

@ComponentPlaceholderInserting
fun testComp(string1: String, string2: String, vararg resolvers: TagResolver): Pair<Component, Component> {
    return Pair(string1.toComp(*resolvers), string2.toComp(*resolvers))
}

@ComponentPlaceholderInserting
fun testCompVararg(strings: Array<out String>, vararg resolvers: TagResolver): Array<Component> {
    return strings.map { it.toComp(*resolvers) }.toTypedArray()
}

@ComponentPlaceholderInserting
fun testCompVararg(vararg strings: String): Array<Component> {
    return testCompVararg(strings)
}