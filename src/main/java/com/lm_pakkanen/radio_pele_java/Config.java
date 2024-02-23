package com.lm_pakkanen.radio_pele_java;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.NonNull;

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
  public @NonNull Boolean REGEN_COMMANDS = false;

  @Value("${BOT_TOKEN}")
  public @NonNull String BOT_TOKEN = "";

  @Value("${BOT_STATUS_MESSAGE}")
  public @NonNull String BOT_STATUS_MESSAGE = "";

  @Value("${SPOTIFY_CLIENT_ID}")
  public @NonNull String SPOTIFY_CLIENT_ID = "";

  @Value("${SPOTIFY_CLIENT_SECRET}")
  public @NonNull String SPOTIFY_CLIENT_SECRET = "";

  /**
   * Generates JDA client instance.
   * 
   * @param commands       autowired commands to listen to.
   * @param eventListeners autowired event listeners to listen to.
   * @return JDA instance.
   */
  @Bean
  public JDA getJDAInstance(@Autowired List<ICommandListener> commands,
      @Autowired List<IEventListener> eventListeners) {

    final JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN);
    builder.setStatus(OnlineStatus.ONLINE);
    builder.setActivity(Activity.playing(BOT_STATUS_MESSAGE));

    final JDA jda = builder.build();
    commands.stream().forEach(jda::addEventListener);
    eventListeners.stream().forEach(jda::addEventListener);

    if (this.REGEN_COMMANDS) {
      // Regenerate commands if env variable is set to true.
      final CommandBuilder commandBuilder = new CommandBuilder(commands);
      commandBuilder.autoGenerateCommands();

      final List<SlashCommandData> generatedSlashCommands = commandBuilder
          .getSlashCommands();

      jda.updateCommands().addCommands(generatedSlashCommands).queue();
    }

    return jda;
  }
}
