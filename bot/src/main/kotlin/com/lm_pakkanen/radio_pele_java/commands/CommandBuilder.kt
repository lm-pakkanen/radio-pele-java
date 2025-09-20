package com.lm_pakkanen.radio_pele_java.commands

import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * @param commandListeners to be used to generate commands (autowired).
 */
class CommandBuilder(
    private val commandListeners: Array<ICommandListener>,
) {
    val slashCommands: Array<SlashCommandData?> = arrayOfNulls(commandListeners.size)

    /**
     * Automatically generates commands and adds them to the list of commands.
     */
    fun autoGenerateCommands() {
        for (i in this.commandListeners.indices) {
            this.slashCommands[i] = this.commandListeners[i].commandData
    }
  }
}
