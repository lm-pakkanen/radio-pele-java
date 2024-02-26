package com.lm_pakkanen.radio_pele_java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@ComponentScan(basePackages = {
    "com.lm_pakkanen.radio_pele_java.controllers",
    "com.lm_pakkanen.radio_pele_java.models"
})
@PropertySource("classpath:application.properties")
public class Config {

  private final Logger LOGGER = LogManager.getLogger();

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
  public JDA getJDAInstance(@Autowired ICommandListener[] commands,
      @Autowired IEventListener[] eventListeners) throws NullPointerException {

    if (commands == null) {
      throw new NullPointerException();
    }

    LOGGER.info("Creating JDA instance.");

    final JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN);

    LOGGER.info("Logged in to the JDA instance.");

    builder.setStatus(OnlineStatus.ONLINE);
    builder.setActivity(Activity.playing(BOT_STATUS_MESSAGE));

    final JDA jda = builder.build();

    for (ICommandListener command : commands) {
      jda.addEventListener(command);
    }

    for (IEventListener eventListener : eventListeners) {
      jda.addEventListener(eventListener);
    }

    LOGGER.info("JDA instance commands built.");

    if (this.REGEN_COMMANDS) {
      LOGGER.info("Regenerating JDA commands.");

      // Regenerate commands if env variable is set to true.
      final CommandBuilder commandBuilder = new CommandBuilder(commands);
      commandBuilder.autoGenerateCommands();

      final SlashCommandData[] generatedSlashCommands = commandBuilder
          .getSlashCommands();

      jda.updateCommands().addCommands(generatedSlashCommands).queue();

      LOGGER.info("JDA commands regenerated.");
    }

    LOGGER.info("JDA instance created.");

    return jda;
  }
}
