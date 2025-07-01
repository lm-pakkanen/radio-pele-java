package com.lm_pakkanen.radio_pele_java.commands

import com.lm_pakkanen.radio_pele_java.controllers.MailMan
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed
import com.lm_pakkanen.radio_pele_java.models.message_embeds.QueueShuffledEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.springframework.stereotype.Component

@Component
class ShuffleCommand(private val trackScheduler: TrackScheduler) : BaseCommand(), ICommandListener {

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
