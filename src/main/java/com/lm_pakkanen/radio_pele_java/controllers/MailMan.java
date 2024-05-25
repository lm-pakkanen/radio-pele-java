package com.lm_pakkanen.radio_pele_java.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class MailMan {

  private final static Logger logger = LoggerFactory.getLogger(MailMan.class);

  /**
   * Send a new message to specified text channel.
   * 
   * @param textChan to reply to.
   * @param message  to reply with.
   */
  public static void send(@Nullable TextChannel textChan,
      @NonNull String message) {
    if (textChan == null) {
      logger.error("TextChannel is null.");
      return;
    }

    logger.info(String.format("Sending message to channel '%s': %s",
        textChan.getName(), message));

    textChan.sendMessage(message).queue();
  }

  /**
   * Send a new embed to specified text channel.
   * 
   * @param textChan to reply to.
   * @param embed    to reply with.
   */
  public static void send(@Nullable TextChannel textChan,
      @NonNull MessageEmbed embed) {
    if (textChan == null) {
      logger.error("TextChannel is null.");
      return;
    }

    logger.info(String.format("Sending message to channel '%s': %s",
        textChan.getName(), embed.toString()));

    textChan.sendMessageEmbeds(embed).queue();
  }

  /**
   * Replies to an interaction with a message. Edits original message if already
   * acknowledged.
   * 
   * @param event   to reply to.
   * @param message to reply with.
   */
  public static void replyInteraction(
      @NonNull SlashCommandInteractionEvent event, @NonNull String message) {

    logger.info(String.format("Replying to message: %s", message));

    if (event.isAcknowledged()) {
      event.getHook().editOriginal(message).queue();
    } else {
      event.reply(message).queue();
    }
  }

  /**
   * Replies to an interaction with an embed. Edits original embed if already
   * acknowledged.
   * 
   * @param event to reply to.
   * @param embed to reply with.
   */
  public static void replyInteraction(
      @NonNull SlashCommandInteractionEvent event,
      @NonNull MessageEmbed embed) {

    logger.info(String.format("Replying to message: %s", embed.toString()));

    if (event.isAcknowledged()) {
      event.getHook().editOriginalEmbeds(embed).queue();
    } else {
      event.replyEmbeds(embed).queue();
    }
  }
}
