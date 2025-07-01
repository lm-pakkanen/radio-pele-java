package com.lm_pakkanen.radio_pele_java.models.message_embeds

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

class ExceptionEmbed(exception: Throwable) : IEmbedBuilder {

  override val embed: MessageEmbed

  init {
    val embedBuilder = IEmbedBuilder.getEmbedBuilder()
    val embedTitle = "Error: ${exception.message}"
    embedBuilder.setAuthor("Radio Pele")
    embedBuilder.setTitle(embedTitle)
    embedBuilder.setColor(Color.RED)
    this.embed = embedBuilder.build()
  }
}
