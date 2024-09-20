package com.lm_pakkanen.radio_pele_java.controllers;

import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Log4j2
public final class MailMan {

  /**
   * Send a new message to specified text channel.
   * 
   * @param textChan to reply to.
   * @param message  to reply with.
   */
  public static void send(Optional<TextChannel> textChanOpt, String message) {
    if (textChanOpt.isEmpty()) {
      log.error("TextChannel is null.");
      return;
    }

    final TextChannel textChan = textChanOpt.get();
    log.info(String.format("Sending message to channel '%s': %s",
        textChan.getName(), message));
    textChan.sendMessage(message).queue();
  }

  /**
   * Send a new embed to specified text channel.
   * 
   * @param textChanOpt to reply to.
   * @param embed       to reply with.
   */
  public static void send(Optional<TextChannel> textChanOpt,
      MessageEmbed embed) {
    if (textChanOpt.isEmpty()) {
      log.error("TextChannel is null.");
      return;
    }

    final TextChannel textChan = textChanOpt.get();

    log.info(String.format("Sending message to channel '%s': %s",
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
  public static void replyInteraction(SlashCommandInteractionEvent event,
      String message) {

    log.info(String.format("Replying to message: %s", message));

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
  public static void replyInteraction(SlashCommandInteractionEvent event,
      MessageEmbed embed) {

    log.info(String.format("Replying to message: %s", embed.toString()));

    if (event.isAcknowledged()) {
      event.getHook().editOriginalEmbeds(embed).queue();
    } else {
      event.replyEmbeds(embed).queue();
    }
  }
}
