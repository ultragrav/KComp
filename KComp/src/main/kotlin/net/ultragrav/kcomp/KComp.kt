package net.ultragrav.kcomp

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object KComp {
    private var resolvers = mutableListOf<TagResolver>()

    lateinit var miniMessage: MiniMessage
        private set

    init {
        resolvers.add(ShorthandTagResolver)
        regenerateMiniMessage()
    }

    private fun regenerateMiniMessage() {
        miniMessage = MiniMessage.builder()
            .editTags { it.resolvers(*resolvers.toTypedArray()) }
            .build()
    }

    fun addTagResolver(resolver: TagResolver) {
        resolvers.add(resolver)
        regenerateMiniMessage()
    }
}

@ComponentPlaceholderInserting
fun String.toComp(vararg replacer: TagResolver): Component = KComp.miniMessage.deserialize(this, *replacer)

@ComponentPlaceholderInserting
fun Collection<String>.toComp(vararg replacer: TagResolver): List<Component> = this.map { it.toComp(*replacer) }

@ComponentPlaceholderInserting
fun String.toCompList(vararg replacer: TagResolver): List<Component> = this.split("\n").toComp(*replacer)

fun Int.toComp() = Component.text(this)
fun Double.toComp() = Component.text(this)
fun Float.toComp() = Component.text(this)
fun Long.toComp() = Component.text(this)
fun Short.toComp() = Component.text(this.toString())
