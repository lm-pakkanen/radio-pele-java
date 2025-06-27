package com.lm_pakkanen.radio_pele_java.models.message_embeds;

import java.awt.Color;
import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public final class ExceptionEmbed implements IEmbedBuilder {

  private final MessageEmbed embed;

  /**
   * @param exception instance.
   */
  public ExceptionEmbed(Throwable exception) {
    final EmbedBuilder embedBuilder = new EmbedBuilder();

    final StringBuilder embedTitleBuilder = new StringBuilder();
    embedTitleBuilder.append("Error: ");
    embedTitleBuilder.append(exception.getMessage());

    final String embedTitle = embedTitleBuilder.toString();

    embedBuilder.setAuthor("Radio Pele");
    embedBuilder.setTitle(embedTitle);
    embedBuilder.setColor(Color.RED);

    this.embed = embedBuilder.build();
  }

}
