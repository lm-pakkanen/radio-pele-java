package com.lmpakkanen.radiopelejava.commands

import com.lmpakkanen.radiopelejava.controllers.MailMan
import com.lmpakkanen.radiopelejava.controllers.TrackScheduler
import com.lmpakkanen.radiopelejava.interfaces.ICommandListener
import com.lmpakkanen.radiopelejava.models.exceptions.InvalidChannelException
import com.lmpakkanen.radiopelejava.models.messages.embeds.ExceptionEmbed
import com.lmpakkanen.radiopelejava.models.messages.embeds.QueueShuffledEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Component

@Component
class ShuffleCommand(
    private val trackScheduler: TrackScheduler,
) : BaseCommand(),
    ICommandListener {
    override val commandName = "shuffle"
    override val commandDescription = "Shuffle the Q."
    override val commandData = Commands.slash(this.commandName, this.commandDescription)

    /**
     * Shuffles the current queue.
     *
     * @throws NullPointerException
     */
    @Throws(NullPointerException::class)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != this.commandName) {
            return
        }

        try {
            super.getTextChan(event)
            this.trackScheduler.shuffle()
            MailMan.replyInteraction(event, QueueShuffledEmbed().embed)
        } catch (exception: InvalidChannelException) {
            MailMan.replyInteraction(event, ExceptionEmbed(exception).embed)
        }
    }
}
