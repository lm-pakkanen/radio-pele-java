package com.lm_pakkanen.radio_pele_java.models.message_embeds;

import java.awt.Color;

import org.springframework.lang.NonNull;

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public final class ExceptionEmbed implements IEmbedBuilder {
  private final @NonNull MessageEmbed embed;

  /**
   * @param exception instance.
   */
  public ExceptionEmbed(@NonNull Throwable exception) {
    final EmbedBuilder embedBuilder = new EmbedBuilder();

    final StringBuilder embedTitleBuilder = new StringBuilder();
    embedTitleBuilder.append("Error: ");
    embedTitleBuilder.append(exception.getMessage());

    final String embedTitle = embedTitleBuilder.toString();

    embedBuilder.setAuthor("Radio Pele");
    embedBuilder.setTitle(embedTitle);
    embedBuilder.setColor(Color.RED);

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
