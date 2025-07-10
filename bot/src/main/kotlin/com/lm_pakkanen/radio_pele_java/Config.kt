package com.lm_pakkanen.radio_pele_java

import com.lm_pakkanen.radio_pele_java.commands.CommandBuilder
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener
import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener
import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.NodeOptions
import dev.arbjerg.lavalink.client.getUserIdFromToken
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener
import lombok.extern.log4j.Log4j2
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.util.Assert

@Log4j2
@Configuration
@ComponentScan(
  basePackages = ["com.lm_pakkanen.radio_pele_java.controllers", "com.lm_pakkanen.radio_pele_java.models"
  ]
)
open class Config(
) {

  companion object {
    const val PLAYLIST_MAX_SIZE: Int = 100
  }

  @Value($$"${REGEN_COMMANDS:false}")
  var regenCommands: Boolean = false

  @Value($$"${LAVALINK_PASSWORD:}")
  var lavalinkPassword: String = ""

  @Value($$"${BOT_TOKEN:}")
  var botToken: String = ""

  @Value($$"${BOT_STATUS_MESSAGE:}")
  var botStatusMessage: String = ""

  @JvmField
  @Value($$"${SPOTIFY_CLIENT_ID:}")
  var spotifyClientId: String = ""

  @JvmField
  @Value($$"${SPOTIFY_CLIENT_SECRET:}")
  var spotifyClientSecret: String = ""

  @JvmField
  @Value($$"${TIDAL_CLIENT_ID:}")
  var tidalClientId: String = ""

  @JvmField
  @Value($$"${TIDAL_CLIENT_SECRET:}")
  var tidalClientSecret: String = ""

  @Bean
  open fun getLavalinkClient(): LavalinkClient {
    val userId = getUserIdFromToken(botToken)
    val client = LavalinkClient(userId)

    val primaryNodeOptions = NodeOptions.Builder()
      .setName("primary")
      .setServerUri("http://lavalink:2333")
      .setPassword(lavalinkPassword)
      .build()

    client.addNode(primaryNodeOptions)
    // TODO handle node connection errors.
    
    return client
  }

  /**
   * Generates JDA client instance.
   *
   * @param commands       autowired commands to listen to.
   * @param eventListeners autowired event listeners to listen to.
   * @return JDA instance.
   */
  @Bean
  open fun getJDAInstance(
    @Autowired lavalinkClient: LavalinkClient,
    @Autowired commands: Array<ICommandListener>,
    @Autowired eventListeners: Array<IEventListener>
  ): JDA {

    Assert.isTrue(commands.isNotEmpty(), "Commands cannot be empty.")

    val clientBuilder = JDABuilder.createDefault(botToken)

    clientBuilder.setVoiceDispatchInterceptor(
      JDAVoiceUpdateListener(lavalinkClient)
    )

    clientBuilder.setStatus(OnlineStatus.ONLINE)
    clientBuilder.setActivity(Activity.playing(botStatusMessage))
    clientBuilder.enableCache(CacheFlag.VOICE_STATE)

    val client = clientBuilder.build()

    for (command in commands) {
      client.addEventListener(command)
    }

    for (eventListener in eventListeners) {
      client.addEventListener(eventListener)
    }

    if (this.regenCommands) {

      // Regenerate commands if env variable is set to true.
      val commandBuilder = CommandBuilder(commands)
      commandBuilder.autoGenerateCommands()

      val generatedSlashCommands = commandBuilder
        .slashCommands

      client.updateCommands().addCommands(*generatedSlashCommands).queue()
    }

    return client
  }
}
