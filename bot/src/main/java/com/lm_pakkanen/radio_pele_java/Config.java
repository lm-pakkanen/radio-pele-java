package com.lm_pakkanen.radio_pele_java;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import com.lm_pakkanen.radio_pele_java.commands.CommandBuilder;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Log4j2
@Configuration
@ComponentScan(basePackages = {
    "com.lm_pakkanen.radio_pele_java.controllers",
    "com.lm_pakkanen.radio_pele_java.models"
})
public class Config {

  public static final int PLAYLIST_MAX_SIZE = 100;

  @Value("${REGEN_COMMANDS:false}")
  public boolean regenCommands = false;

  @Value("${LAVALINK_PASSWORD:}")
  public String lavalinkPassword = "";

  @Value("${BOT_TOKEN:}")
  public String botToken = "";

  @Value("${BOT_STATUS_MESSAGE:}")
  public String botStatusMessage = "";

  @Value("${SPOTIFY_CLIENT_ID:}")
  public String spotifyClientId = "";

  @Value("${SPOTIFY_CLIENT_SECRET:}")
  public String spotifyClientSecret = "";

  @Value("${TIDAL_CLIENT_ID:}")
  public String tidalClientId = "";

  @Value("${TIDAL_CLIENT_SECRET:}")
  public String tidalClientSecret = "";

  @Bean
  LavalinkClient getLavalinkClient() {

    final long userId = Helpers.getUserIdFromToken(botToken);
    final LavalinkClient client = new LavalinkClient(userId);

    final NodeOptions primaryNodeOptions = new NodeOptions.Builder()
        .setName("primary").setServerUri("http://localhost:2333")
        .setPassword(lavalinkPassword).build();

    final LavalinkNode node = client.addNode(primaryNodeOptions);
    // TODO handle node connection errors.

    return client;
  }

  /**
   * Generates JDA client instance.
   * 
   * @param commands       autowired commands to listen to.
   * @param eventListeners autowired event listeners to listen to.
   * @return JDA instance.
   */
  @Bean
  JDA getJDAInstance(@Autowired LavalinkClient lavalinkClient,
      @Autowired ICommandListener[] commands,
      @Autowired IEventListener[] eventListeners) {

    final ICommandListener[] nonNullCommands = Optional.ofNullable(commands)
        .orElseThrow(
            () -> new IllegalArgumentException("Commands cannot be null."));

    Assert.isTrue(nonNullCommands.length > 0, "Commands cannot be empty.");

    log.info("Creating JDA instance.");
    final JDABuilder clientBuilder = JDABuilder.createDefault(botToken);
    log.info("Logged in to the JDA instance.");

    log.info("Setting voice dispatch interceptor.");
    clientBuilder.setVoiceDispatchInterceptor(
        new JDAVoiceUpdateListener(lavalinkClient));

    clientBuilder.setStatus(OnlineStatus.ONLINE);
    clientBuilder.setActivity(Activity.playing(botStatusMessage));

    log.info("Building JDA instance.");
    final JDA client = clientBuilder.build();

    log.info("Building JDA instance commands.");

    for (ICommandListener command : nonNullCommands) {
      client.addEventListener(command);
    }

    for (IEventListener eventListener : eventListeners) {
      client.addEventListener(eventListener);
    }

    log.info("JDA instance commands built.");

    if (this.regenCommands) {
      log.info("Regenerating JDA commands.");

      // Regenerate commands if env variable is set to true.
      final CommandBuilder commandBuilder = new CommandBuilder(nonNullCommands);
      commandBuilder.autoGenerateCommands();

      final SlashCommandData[] generatedSlashCommands = commandBuilder
          .getSlashCommands();

      client.updateCommands().addCommands(generatedSlashCommands).queue();

      log.info("JDA commands regenerated.");
    }

    log.info("JDA instance created.");
    return client;
  }
}
