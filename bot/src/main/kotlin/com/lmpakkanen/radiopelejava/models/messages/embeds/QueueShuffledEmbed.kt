package com.lmpakkanen.radiopelejava.models.messages.embeds

import com.lmpakkanen.radiopelejava.interfaces.IEmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class QueueShuffledEmbed : IEmbedBuilder {
    override val embed: MessageEmbed

    init {
        val embedBuilder = IEmbedBuilder.getEmbedBuilder()
        embedBuilder.setTitle("Q shuffled.")
        this.embed = embedBuilder.build()
    }
}
