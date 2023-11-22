package net.ultragrav.kcomp

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

private val colors = mapOf(
    'a' to NamedTextColor.GREEN,
    'b' to NamedTextColor.AQUA,
    'c' to NamedTextColor.RED,
    'd' to NamedTextColor.LIGHT_PURPLE,
    'e' to NamedTextColor.YELLOW,
    'f' to NamedTextColor.WHITE,
    '0' to NamedTextColor.BLACK,
    '1' to NamedTextColor.DARK_BLUE,
    '2' to NamedTextColor.DARK_GREEN,
    '3' to NamedTextColor.DARK_AQUA,
    '4' to NamedTextColor.DARK_RED,
    '5' to NamedTextColor.DARK_PURPLE,
    '6' to NamedTextColor.GOLD,
    '7' to NamedTextColor.GRAY,
    '8' to NamedTextColor.DARK_GRAY,
    '9' to NamedTextColor.BLUE,
)
private val formats = mapOf(
    'k' to TextDecoration.OBFUSCATED,
    'l' to TextDecoration.BOLD,
    'm' to TextDecoration.STRIKETHROUGH,
    'n' to TextDecoration.UNDERLINED,
    'o' to TextDecoration.ITALIC
)

object ShorthandTagResolver : TagResolver {
    override fun resolve(name: String, arguments: ArgumentQueue, ctx: Context): Tag? {
        if (!has(name)) return null

        // Get color and formats
        val ch = name[0]
        val formats = name.filter { it in formats }.map { formats[it]!! }

        return Tag.styling {
            if (ch == 'r') {
                it.color(null)
                TextDecoration.values().forEach { decoration ->
                    it.decoration(decoration, false)
                }
            }
            val color = colors[ch]
            if (color != null) {
                it.color(color)
                TextDecoration.values().forEach { decoration ->
                    it.decoration(decoration, formats.contains(decoration))
                }
            } else {
                formats.forEach { decoration ->
                    it.decoration(decoration, true)
                }
            }
        }
    }

    override fun has(name: String): Boolean {
        if (name.isEmpty()) return false

        return (name[0] == 'r' || name[0] in colors || name[0] in formats) && name.substring(1).all { it in formats }
    }
}