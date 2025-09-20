package com.lmpakkanen.radiopelejava.commands

import com.lmpakkanen.radiopelejava.controllers.MailMan
import com.lmpakkanen.radiopelejava.controllers.TrackScheduler
import com.lmpakkanen.radiopelejava.interfaces.ICommandListener
import com.lmpakkanen.radiopelejava.models.Store
import com.lmpakkanen.radiopelejava.models.exceptions.InvalidChannelException
import com.lmpakkanen.radiopelejava.models.messages.embeds.CurrentSongEmbed
import com.lmpakkanen.radiopelejava.models.messages.embeds.ExceptionEmbed
import com.lmpakkanen.radiopelejava.models.messages.embeds.QueueEmptyEmbed
import com.lmpakkanen.radiopelejava.models.messages.embeds.SongSkippedEmbed
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
