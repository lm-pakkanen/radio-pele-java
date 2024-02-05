package com.lm_pakkanen.radio_pele_java;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.lm_pakkanen.radio_pele_java.controllers.commands.CommandBuilder;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Configuration
@ComponentScan("com.lm_pakkanen.radio_pele_java.controllers")
@PropertySource("classpath:application.yml")
public class Config {

  @Value("${REGEN_COMMANDS}")
  public boolean REGEN_COMMANDS;

  @Value("${BOT_CLIENT_ID}")
  public String BOT_CLIENT_ID;

  @Value("${BOT_TOKEN}")
  public String BOT_TOKEN;

  @Value("${BOT_STATUS_MESSAGE}")
  public String BOT_STATUS_MESSAGE;

  @Value("${SPOTIFY_CLIENT_ID}")
  public String SPOTIFY_CLIENT_ID;

  @Value("${SPOTIFY_CLIENT_SECRET}")
  public String SPOTIFY_CLIENT_SECRET;

  @Bean
  public JDA getJDAInstance(@Autowired List<ICommandListener> commands,
      @Autowired List<IEventListener> eventListeners) {

    JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN);
    builder.setStatus(OnlineStatus.ONLINE);
    builder.setActivity(Activity.playing(BOT_STATUS_MESSAGE));

    JDA jda = builder.build();
    commands.stream().forEach(jda::addEventListener);
    eventListeners.stream().forEach(jda::addEventListener);

    if (this.REGEN_COMMANDS) {
      CommandBuilder commandBuilder = new CommandBuilder(commands);
      commandBuilder.autoGenerateCommands();

      List<SlashCommandData> generatedSlashCommands = commandBuilder
          .getSlashCommands();

      jda.updateCommands().addCommands(generatedSlashCommands).queue();
    }

    return jda;
  }
}
