package com.lm_pakkanen.radio_pele_java.commands;

import org.springframework.stereotype.Component;
import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.QueueShuffledEmbed;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
@RequiredArgsConstructor
public final class ShuffleCommand extends BaseCommand
    implements ICommandListener {

  private final TrackScheduler trackScheduler;

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
   * 
   * @throws NullPointerException
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
      MailMan.replyInteraction(event, new QueueShuffledEmbed().getEmbed());
    } catch (InvalidChannelException exception) {
      MailMan.replyInteraction(event, new ExceptionEmbed(exception).getEmbed());
    }
  }
}
