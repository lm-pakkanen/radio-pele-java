package com.lm_pakkanen.radio_pele_java.models.message_embeds;

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public final class SongSkippedEmbed implements IEmbedBuilder {

  private final MessageEmbed embed;

  public SongSkippedEmbed() {
    final EmbedBuilder embedBuilder = IEmbedBuilder.getEmbedBuilder();
    embedBuilder.setTitle("Song skipped.");
    this.embed = embedBuilder.build();
  }
}
