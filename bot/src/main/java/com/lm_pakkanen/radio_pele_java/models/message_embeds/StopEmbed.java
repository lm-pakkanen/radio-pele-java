package com.lm_pakkanen.radio_pele_java.models.message_embeds;

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public final class StopEmbed implements IEmbedBuilder {

  private final MessageEmbed embed;

  public StopEmbed() {
    final EmbedBuilder embedBuilder = IEmbedBuilder.getEmbedBuilder();
    embedBuilder.setTitle("Hasta la Vista!");
    this.embed = embedBuilder.build();
  }
}
