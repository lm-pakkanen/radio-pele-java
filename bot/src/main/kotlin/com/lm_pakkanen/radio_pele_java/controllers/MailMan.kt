package com.lm_pakkanen.radio_pele_java.controllers

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MailMan {

  companion object {

    /**
     * Send a new message to specified text channel.
     *
     * @param textChan to reply to.
     * @param message     to reply with.
     */
    fun send(textChan: TextChannel?, message: String) {

      if (textChan == null) {
        return
      }

      textChan.sendMessage(message).queue()
    }

    /**
     * Send a new embed to specified text channel.
     *
     * @param textChan to reply to.
     * @param embed       to reply with.
     */
    fun send(
      textChan: TextChannel?,
      embed: MessageEmbed
    ) {

      if (textChan == null) {
        return
      }

      textChan.sendMessageEmbeds(embed).queue()
    }

    /**
     * Replies to an interaction with a message. Edits original message if already
     * acknowledged.
     *
     * @param event   to reply to.
     * @param message to reply with.
     */
    fun replyInteraction(
      event: SlashCommandInteractionEvent,
      message: String
    ) {
      if (event.isAcknowledged) {
        event.hook.editOriginal(message).queue()
      } else {
        event.reply(message).queue()
      }
    }

    /**
     * Replies to an interaction with an embed. Edits original embed if already
     * acknowledged.
     *
     * @param event to reply to.
     * @param embed to reply with.
     */
    fun replyInteraction(
      event: SlashCommandInteractionEvent,
      embed: MessageEmbed
    ) {
      if (event.isAcknowledged) {
        event.hook.editOriginalEmbeds(embed).queue()
      } else {
        event.replyEmbeds(embed).queue()
      }
    }
  }
}
