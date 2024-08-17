package com.lm_pakkanen.radio_pele_java;

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

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Slf4j
@Configuration
@ComponentScan(basePackages = {
    "com.lm_pakkanen.radio_pele_java.controllers",
    "com.lm_pakkanen.radio_pele_java.models"
})
@PropertySource("classpath:application.properties")
public class Config {

  public final static int PLAYLIST_MAX_SIZE = 500;

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

  @Value("${TIDAL_CLIENT_ID}")
  public @NonNull String TIDAL_CLIENT_ID = "";

  @Value("${TIDAL_CLIENT_SECRET}")
  public @NonNull String TIDAL_CLIENT_SECRET = "";

  /**
   * Generates JDA client instance.
   * 
   * @param commands       autowired commands to listen to.
   * @param eventListeners autowired event listeners to listen to.
   * @return JDA instance.
   */
  @Bean
  public JDA getJDAInstance(@Autowired ICommandListener[] commands,
      @Autowired IEventListener[] eventListeners) throws NullPointerException {

    if (commands == null) {
      throw new NullPointerException();
    }

    log.info("Creating JDA instance.");

    final JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN);

    log.info("Logged in to the JDA instance.");

    builder.setStatus(OnlineStatus.ONLINE);
    builder.setActivity(Activity.playing(BOT_STATUS_MESSAGE));

    final JDA jda = builder.build();

    for (ICommandListener command : commands) {
      jda.addEventListener(command);
    }

    for (IEventListener eventListener : eventListeners) {
      jda.addEventListener(eventListener);
    }

    log.info("JDA instance commands built.");

    if (this.REGEN_COMMANDS) {
      log.info("Regenerating JDA commands.");

      // Regenerate commands if env variable is set to true.
      final CommandBuilder commandBuilder = new CommandBuilder(commands);
      commandBuilder.autoGenerateCommands();

      final SlashCommandData[] generatedSlashCommands = commandBuilder
          .getSlashCommands();

      jda.updateCommands().addCommands(generatedSlashCommands).queue();

      log.info("JDA commands regenerated.");
    }

    log.info("JDA instance created.");

    return jda;
  }
}
