package com.lm_pakkanen.radio_pele_java.util

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.player.LavalinkPlayer
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import java.util.Optional


@Component
class LavaLinkUtil : ApplicationContextAware {

  companion object {

    private lateinit var client: LavalinkClient

    fun getPlayer(guildId: Long?): LavalinkPlayer {
      val link: Link = getLink(guildId)
      return Optional.ofNullable(link.cachedPlayer)
        .orElse(link.createOrUpdatePlayer().block())
    }

    fun getLink(guildId: Long?): Link {
      Assert.notNull(guildId, "Guild ID must not be null.")
      return Optional.ofNullable(client.getLinkIfCached(guildId!!))
        .orElse(client.getOrCreateLink(guildId))
    }
  }

  override fun setApplicationContext(
    applicationContext: ApplicationContext
  ) {
    client = applicationContext.getBean(LavalinkClient::class.java)
  }
}


