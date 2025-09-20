package com.lm_pakkanen.radio_pele_java.commands

import com.lm_pakkanen.radio_pele_java.controllers.MailMan
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener
import com.lm_pakkanen.radio_pele_java.models.Store
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException
import com.lm_pakkanen.radio_pele_java.models.message_embeds.CurrentSongEmbed
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed
import com.lm_pakkanen.radio_pele_java.models.message_embeds.QueueEmptyEmbed
import com.lm_pakkanen.radio_pele_java.models.message_embeds.SongSkippedEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Lazy
@Component
class SkipCommand(
    private val store: Store,
    private val trackScheduler: TrackScheduler,
) : BaseCommand(),
    ICommandListener {
    override val commandName = "skip"
    override val commandDescription = "Skip the current song."
    override val commandData = Commands.slash(this.commandName, this.commandDescription)

    /** Skips the current song. */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != commandName) {
            return
        }

        try {
            check(trackScheduler.isPlaying) { "No song to skip!" }

            val textChan = super.getTextChan(event)
            val nextTrack = this.trackScheduler.skipCurrentSong()

            MailMan.replyInteraction(event, SongSkippedEmbed().embed)

            nextTrack?.let { nextTrack ->
                MailMan.send(
                    textChan,
                    CurrentSongEmbed(nextTrack, store).embed,
                )
            } ?: run {
                MailMan.send(textChan, QueueEmptyEmbed().embed)
            }
        } catch (ex: Exception) {
            when (ex) {
                is InvalidChannelException, is IllegalStateException -> {
          MailMan.replyInteraction(event, ExceptionEmbed(ex).embed)
        }

        else -> throw ex
      }
    }
  }
}
