package com.lmpakkanen.radiopelejava.models.messages.embeds

import com.lmpakkanen.radiopelejava.interfaces.IEmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class ExceptionEmbed(
    exception: Throwable,
) : IEmbedBuilder {
    override val embed: MessageEmbed

    init {
        val embedBuilder = IEmbedBuilder.getEmbedBuilder(IEmbedBuilder.ERROR_COLOR)
        val embedTitle = "Error: ${exception.message}"
        embedBuilder.setAuthor("Radio Pele")
        embedBuilder.setTitle(embedTitle)
        this.embed = embedBuilder.build()
    }
}
