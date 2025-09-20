package com.lm_pakkanen.radio_pele_java.commands

import com.lm_pakkanen.radio_pele_java.controllers.MailMan
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener
import com.lm_pakkanen.radio_pele_java.models.Store
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException
import com.lm_pakkanen.radio_pele_java.models.exceptions.NotInChannelException
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed
import com.lm_pakkanen.radio_pele_java.models.message_embeds.SongAddedEmbed
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Lazy
@Component
class PlayListCommand(
    private val store: Store,
    private val trackScheduler: TrackScheduler,
) : BaseCommand(),
    ICommandListener {
    override val commandName = "playlist"
    override val commandDescription = "Play playlist from a given URL."

    override val commandData: SlashCommandData =
        Commands
            .slash(this.commandName, this.commandDescription)
            .addOption(
                OptionType.STRING,
                "url",
                "The URL of the playlist to play",
                true,
                false,
            )

    /**
     * Connects to the voice channel, adds the requested song to the queue and
     * starts playback if necessary.
     *
     * @param event that initiated the command.
     * @throws NullPointerException
     */
    @Throws(NullPointerException::class)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != this.commandName) {
            return
        }

        event.deferReply().queue()

        try {
            val textChan = super.getTextChan(event)

            val guild: Guild =
                event.guild
                    ?: throw IllegalArgumentException("Guild cannot be null")

            val guildId = guild.idLong
            val url = event.getOption("url")!!.asString

            val addedTrack =
                this.trackScheduler.addToQueue(
                    textChan,
                    guildId,
                    url,
                    false,
                )

            val audioManager = guild.audioManager
            connectToVoiceChan(event)

            if (!audioManager.isSelfDeafened) {
                audioManager.isSelfDeafened = true
            }

            if (!this.trackScheduler.isPlaying) {
                this.trackScheduler.play()
            }

            MailMan.replyInteraction(
                event,
                SongAddedEmbed(addedTrack, this.store).embed,
            )
        } catch (ex: Exception) {
            when (ex) {
                is InvalidChannelException, is NotInChannelException, is FailedToLoadSongException -> {
                    MailMan.replyInteraction(event, ExceptionEmbed(ex).embed)
                }

                else -> throw ex
            }
        }
    }

    @Throws(NotInChannelException::class)
    private fun connectToVoiceChan(event: SlashCommandInteractionEvent) {
        val memberVoiceChannel: AudioChannel = tryGetMemberVoiceChan(event)
        event.jda.directAudioController.connect(memberVoiceChannel)
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
