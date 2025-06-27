package com.lm_pakkanen.radio_pele_java.commands;

import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class BaseCommand extends ListenerAdapter {

  /**
   * @param event which initiated the command.
   * @return the text channel where the command was initiated.
   * @throws InvalidChannelException
   */
  public TextChannel getTextChan(SlashCommandInteractionEvent event)
      throws InvalidChannelException {

    final MessageChannelUnion messageChan = event.getChannel();

    if (!messageChan.getType().equals(ChannelType.TEXT)) {
      throw new InvalidChannelException();
    }

    return messageChan.asTextChannel();
  }
}
