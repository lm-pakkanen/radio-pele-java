package com.lm_pakkanen.radio_pele_java.controllers.commands;

import org.springframework.lang.NonNull;

import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BaseCommand extends ListenerAdapter {
  public @NonNull TextChannel getTextChan(SlashCommandInteractionEvent event)
      throws InvalidChannelException {
    final MessageChannelUnion messageChan = event.getChannel();

    if (!messageChan.getType().equals(ChannelType.TEXT)) {
      throw new InvalidChannelException();
    }

    final TextChannel textChan = messageChan.asTextChannel();

    if (textChan == null) {
      throw new InvalidChannelException();
    }

    return textChan;
  }
}
