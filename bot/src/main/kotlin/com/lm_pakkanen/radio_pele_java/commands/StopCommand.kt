package com.lm_pakkanen.radio_pele_java.commands

import com.lm_pakkanen.radio_pele_java.controllers.MailMan
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException
import com.lm_pakkanen.radio_pele_java.models.exceptions.NotInChannelException
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed
import com.lm_pakkanen.radio_pele_java.models.message_embeds.StopEmbed
import lombok.extern.log4j.Log4j2
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Component

@Log4j2
@Component
class StopCommand(
  private val trackScheduler: TrackScheduler
) : BaseCommand(), ICommandListener {

  override val commandName = "stop"
  override val commandDescription = "Stop playback and leave."
  override val commandData = Commands.slash(this.commandName, this.commandDescription)

  /** Stops playback and leaves the voice channel. */
  override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {

    if (event.name != this.commandName) {
      return
    }

    try {
      super.getTextChan(event)

      this.trackScheduler.destroy()
      disconnectFromVoiceChan(event)

      MailMan.replyInteraction(event, StopEmbed().embed)
    } catch (exception: InvalidChannelException) {
      MailMan.replyInteraction(event, ExceptionEmbed(exception).embed)
    }
  }

  private fun disconnectFromVoiceChan(event: SlashCommandInteractionEvent) {
    try {
      val memberVoiceChannel: AudioChannel = tryGetMemberVoiceChan(event)
      event.jda.directAudioController.disconnect(memberVoiceChannel.guild)
    } catch (ex: Exception) {
      // TODO
    }
  }

  /**
   * Tries to get the voice channel of the member who initiated the command.
   *
   * @param event that initiated the command.
   * @throws NotInChannelException
   */
  @Throws(NotInChannelException::class)
  private fun tryGetMemberVoiceChan(event: SlashCommandInteractionEvent): AudioChannel {

    val member: Member = event.member ?: throw IllegalStateException("Member cannot be null")
    val memberVoiceState = member.voiceState ?: throw NotInChannelException()

    if (!memberVoiceState.inAudioChannel()) {
      throw NotInChannelException()
    }

    return memberVoiceState.channel!!
  }
}
