package com.lm_pakkanen.radio_pele_java.commands;

import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class CommandBuilder {
  private ICommandListener[] commandListeners;
  private SlashCommandData[] slashCommands;

  /**
   * @param commandListeners to be used to generate commands (autowired).
   */
  public CommandBuilder(ICommandListener[] commandListeners) {
    this.commandListeners = commandListeners;
    this.slashCommands = new SlashCommandData[commandListeners.length];
  }

  /**
   * Automatically generates commands and adds them to the list of commands.
   */
  public void autoGenerateCommands() {
    for (int i = 0; i < this.commandListeners.length; i++) {
      this.slashCommands[i] = this.commandListeners[i].getCommandData();
    }
  }

  /**
   * @return the built list of slash commands.
   */
  public SlashCommandData[] getSlashCommands() {
    return this.slashCommands;
  }
}
