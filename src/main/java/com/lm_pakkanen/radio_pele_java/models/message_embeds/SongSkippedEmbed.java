package com.lm_pakkanen.radio_pele_java.models.message_embeds;

import org.springframework.lang.NonNull;

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public final class SongSkippedEmbed implements IEmbedBuilder {
  private final @NonNull MessageEmbed embed;

  public SongSkippedEmbed() {

    final EmbedBuilder embedBuilder = IEmbedBuilder.getEmbedBuilder();
    embedBuilder.setTitle("Song skipped.");

    final MessageEmbed embed = embedBuilder.build();

    if (embed == null) {
      throw new NullPointerException("Embed is null");
    }

    this.embed = embed;
  }

  /**
   * @return message embed.
   */
  @Override
  public @NonNull MessageEmbed getEmbed() {
    return this.embed;
  }
}
