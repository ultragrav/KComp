package net.ultragrav.kcomp

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object KComp {
    private var resolvers = mutableListOf<TagResolver>()

    private lateinit var miniMessage: MiniMessage

    private fun regenerateMiniMessage() {
        miniMessage = MiniMessage.builder()
            .editTags { it.resolvers(*resolvers.toTypedArray()) }
            .build()
    }

    fun addTagResolver(resolver: ShorthandTagResolver) {
        resolvers.add(resolver)
        regenerateMiniMessage()
    }

    fun String.toComp(vararg replacer: TagResolver): Component = miniMessage.deserialize(this, *replacer)
    fun Collection<String>.toComp(vararg replacer: TagResolver): List<Component> = this.map { it.toComp(*replacer) }
}
