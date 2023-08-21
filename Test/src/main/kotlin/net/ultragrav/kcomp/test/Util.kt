package net.ultragrav.kcomp.test

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.ultragrav.kcomp.ComponentPlaceholderInserting
import net.ultragrav.kcomp.KComp

@ComponentPlaceholderInserting
fun testComp(string1: String, string2: String, vararg resolvers: TagResolver): Pair<Component, Component> {
    return Pair(KComp.miniMessage.deserialize(string1, *resolvers), KComp.miniMessage.deserialize(string2, *resolvers))
}