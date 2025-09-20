package com.lm_pakkanen.radio_pele_java.models.message_embeds

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class QueueEmptyEmbed : IEmbedBuilder {
    override val embed: MessageEmbed

    init {
        val embedBuilder = IEmbedBuilder.getEmbedBuilder()
        embedBuilder.setTitle("The Q is now empty.")
        this.embed = embedBuilder.build()
  }
}
