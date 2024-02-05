package com.lm_pakkanen.radio_pele_java.interfaces;

import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommandListener extends EventListener {
  public String getCommandName();

  public String getCommandDescription();

  public SlashCommandData getCommandData();
}
