package com.lm_pakkanen.radio_pele_java.models.message_embeds

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder
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
