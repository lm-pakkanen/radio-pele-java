package com.lmpakkanen.radiopelejava.interfaces

import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * Interface for identifying custom command listeners. Defines properties for
 * getting information about the command.
 */
interface ICommandListener : EventListener {
    val commandName: String
    val commandDescription: String
    val commandData: SlashCommandData
}
