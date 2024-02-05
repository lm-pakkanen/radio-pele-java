package com.lm_pakkanen.radio_pele_java.controllers.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public final class ShuffleCommand extends BaseCommand
    implements ICommandListener {

  private @NonNull TrackScheduler trackScheduler;

  public ShuffleCommand(@Autowired @NonNull TrackScheduler trackScheduler) {
    super();
    this.trackScheduler = trackScheduler;
  }

  @Override
  public String getCommandName() {
    return "shuffle";
  }

  @Override
  public String getCommandDescription() {
    return "Shuffle the Q.";
  }

  @Override
  public SlashCommandData getCommandData() {
    return Commands.slash(this.getCommandName(), this.getCommandDescription());
  }

  /**
   * Shuffles the current queue.
   */
  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
      throws NullPointerException {
    if (!event.getName().equals(this.getCommandName())) {
      return;
    }

    try {
      super.getTextChan(event);
      this.trackScheduler.shuffle();
      MailMan.replyInteractionMessage(event, "Q shuffled.");
    } catch (InvalidChannelException exception) {
      String exceptionMessage = exception.getMessage();

      if (exceptionMessage == null) {
        exceptionMessage = "Unknown exception occurred.";
      }

      MailMan.replyInteractionMessage(event, exceptionMessage);
    }
  }
}
