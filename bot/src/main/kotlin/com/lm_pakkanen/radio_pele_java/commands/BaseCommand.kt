package com.lm_pakkanen.radio_pele_java.commands

import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class BaseCommand : ListenerAdapter() {
    /**
     * @param event which initiated the command.
     * @return the text channel where the command was initiated.
     * @throws InvalidChannelException
     */
    @Throws(InvalidChannelException::class)
    fun getTextChan(event: SlashCommandInteractionEvent): TextChannel {
        val messageChan = event.channel

        if (messageChan.type != ChannelType.TEXT) {
            throw InvalidChannelException()
        }

        return messageChan.asTextChannel()
  }
}
