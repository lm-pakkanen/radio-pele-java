package com.lm_pakkanen.radio_pele_java.controllers;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class MailMan {
  public static void sendMessage(@Nullable TextChannel textChan,
      String message) {
    if (textChan == null) {
      return;
    }

    textChan.sendMessage(message).queue();
  }

  public static void sendEmbed(@Nullable TextChannel textChan,
      MessageEmbed embed) {
    if (textChan == null) {
      return;
    }

    textChan.sendMessageEmbeds(embed).queue();
  }

  public static void replyInteractionMessage(
      @NonNull SlashCommandInteractionEvent event, String message) {
    if (event.isAcknowledged()) {
      event.getHook().editOriginal(message).queue();
    } else {
      event.reply(message).queue();
    }
  }

  public static void replyInteractionEmbed(
      @NonNull SlashCommandInteractionEvent event, MessageEmbed embed) {
    if (event.isAcknowledged()) {
      event.getHook().editOriginalEmbeds(embed).queue();
    } else {
      event.replyEmbeds(embed).queue();
    }
  }
}
