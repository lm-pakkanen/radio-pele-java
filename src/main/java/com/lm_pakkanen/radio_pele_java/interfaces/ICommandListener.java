package com.lm_pakkanen.radio_pele_java.interfaces;

import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Interface for identifying custom command listeners. Defines properties for
 * getting information about the command.
 */
public interface ICommandListener extends EventListener {
  /**
   * @return the name of the command.
   */
  public String getCommandName();

  /**
   * @return the description of the command.
   */
  public String getCommandDescription();

  /**
   * @return the command as a SlashCommandData instance.
   */
  public SlashCommandData getCommandData();
}
