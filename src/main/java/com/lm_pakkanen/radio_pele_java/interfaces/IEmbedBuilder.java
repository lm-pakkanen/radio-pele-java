package com.lm_pakkanen.radio_pele_java.interfaces;

import java.awt.Color;

import org.springframework.lang.NonNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public interface IEmbedBuilder {
  public static final Color DEFAULT_COLOR = Color.GREEN;
  public static final Color ERROR_COLOR = Color.RED;

  public static @NonNull EmbedBuilder getEmbedBuilder() {
    return IEmbedBuilder.getEmbedBuilder(DEFAULT_COLOR);
  }

  public static @NonNull EmbedBuilder getEmbedBuilder(Color color) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setAuthor("Radio Pele");
    embedBuilder.setColor(color);
    return embedBuilder;
  }

  public @NonNull MessageEmbed getEmbed();
}
