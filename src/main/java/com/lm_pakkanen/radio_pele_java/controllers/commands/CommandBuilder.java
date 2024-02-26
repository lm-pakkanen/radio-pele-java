package com.lm_pakkanen.radio_pele_java.controllers.commands;

import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;

import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public final class CommandBuilder {
  private @NonNull List<ICommandListener> commandListeners;
  private @NonNull List<SlashCommandData> slashCommands;

  /**
   * @param commandListeners to be used to generate commands (autowired).
   */
  public CommandBuilder(@NonNull List<ICommandListener> commandListeners) {
    this.commandListeners = commandListeners;
    this.slashCommands = new ArrayList<>();
  }

  /**
   * Add a command to the list of commands manually.
   * 
   * @param command to add.
   */
  public void addCommand(@NonNull SlashCommandData command) {
    this.slashCommands.add(command);
  }

  /**
   * Automatically generates commands and adds them to the list of commands.
   */
  public void autoGenerateCommands() {
    for (ICommandListener commandListener : this.commandListeners) {
      this.slashCommands.add(commandListener.getCommandData());
    }
  }

  /**
   * @return the built list of slash commands.
   */
  public @NonNull List<SlashCommandData> getSlashCommands() {
    return this.slashCommands;
  }
}
