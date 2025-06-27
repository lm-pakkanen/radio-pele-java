package com.lm_pakkanen.radio_pele_java.models.message_embeds;

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public final class QueueEmptyEmbed implements IEmbedBuilder {

  private final MessageEmbed embed;

  public QueueEmptyEmbed() {

    final EmbedBuilder embedBuilder = IEmbedBuilder.getEmbedBuilder();
    embedBuilder.setTitle("The Q is now empty.");

    this.embed = embedBuilder.build();
  }

}
