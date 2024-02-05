package com.lm_pakkanen.radio_pele_java.controllers.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public final class SkipCommand extends ListenerAdapter
    implements ICommandListener {

  private TrackScheduler trackScheduler;

  public SkipCommand(@Autowired TrackScheduler trackScheduler) {
    this.trackScheduler = trackScheduler;
  }

  @Override
  public String getCommandName() {
    return "skip";
  }

  @Override
  public String getCommandDescription() {
    return "Skip the current song.";
  }

  @Override
  public SlashCommandData getCommandData() {
    return Commands.slash(this.getCommandName(), this.getCommandDescription());
  }

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
      throws NullPointerException {
    if (!event.getName().equals(this.getCommandName())) {
      return;
    }

    try {
      MessageChannelUnion messageChan = event.getChannel();

      if (!messageChan.getType().equals(ChannelType.TEXT)) {
        throw new InvalidChannelException();
      }

      this.trackScheduler.skipCurrentSong();

      MailMan.replyInteractionMessage(event, "Song skipped.");
    } catch (InvalidChannelException exception) {
      MailMan.replyInteractionMessage(event, exception.getMessage());
    }
  }
}
