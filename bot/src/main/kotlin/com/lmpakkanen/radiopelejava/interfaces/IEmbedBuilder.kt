package com.lmpakkanen.radiopelejava.interfaces

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

interface IEmbedBuilder {
    companion object {
        val DEFAULT_COLOR: Color = Color.GREEN
        val ERROR_COLOR: Color = Color.RED

        fun getEmbedBuilder(): EmbedBuilder = getEmbedBuilder(DEFAULT_COLOR)

        fun getEmbedBuilder(color: Color): EmbedBuilder {
            val embedBuilder = EmbedBuilder()
            embedBuilder.setAuthor("Radio Pele")
            embedBuilder.setColor(color)
            return embedBuilder
        }
    }

    val embed: MessageEmbed
}
