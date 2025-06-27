package com.lm_pakkanen.radio_pele_java.commands;

import org.springframework.stereotype.Component;
import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;
import com.lm_pakkanen.radio_pele_java.models.exceptions.NotInChannelException;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.StopEmbed;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Log4j2
@Component
@RequiredArgsConstructor
public final class StopCommand extends BaseCommand implements ICommandListener {

  private final TrackScheduler trackScheduler;

  @Override
  public String getCommandName() {
    return "stop";
  }

  @Override
  public String getCommandDescription() {
    return "Stop playback and leave.";
  }

  @Override
  public SlashCommandData getCommandData() {
    return Commands.slash(this.getCommandName(), this.getCommandDescription());
  }

  /**
   * Stops playback and leaves the voice channel.
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

      this.trackScheduler.destroy();
      disconnectFromVoiceChan(event);

      MailMan.replyInteraction(event, new StopEmbed().getEmbed());
    } catch (InvalidChannelException exception) {
      MailMan.replyInteraction(event, new ExceptionEmbed(exception).getEmbed());
    }
  }

  private void disconnectFromVoiceChan(SlashCommandInteractionEvent event) {

    try {
      final AudioChannel memberVoiceChannel = tryGetMemberVoiceChan(event);
      event.getJDA().getDirectAudioController()
          .disconnect(memberVoiceChannel.getGuild());
    } catch (Exception ex) {
      log.warn("Failed to disconnect from voice channel: {}", ex.getMessage());
    }

  }

  /**
   * Tries to get the voice channel of the member who initiated the command.
   * 
   * @param event that initiated the command.
   * @throws NotInChannelException
   */
  private AudioChannel tryGetMemberVoiceChan(SlashCommandInteractionEvent event)
      throws NotInChannelException {

    final Member member = event.getMember();
    final GuildVoiceState memberVoiceState = member.getVoiceState();

    final AudioChannelUnion memberChannel = memberVoiceState.getChannel();

    if (!memberVoiceState.inAudioChannel()) {
      throw new NotInChannelException();
    }

    return memberChannel;
  }
}
