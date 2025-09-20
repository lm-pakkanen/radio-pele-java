package com.lmpakkanen.radiopelejava.util

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.player.LavalinkPlayer
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class LavaLinkUtil : ApplicationContextAware {
    companion object {
        private lateinit var client: LavalinkClient

        fun getPlayer(guildId: Long?): LavalinkPlayer {
            val link: Link = getLink(guildId)
            return link.cachedPlayer ?: link.createOrUpdatePlayer().block()
        }

        fun getLink(guildId: Long?): Link {
            checkNotNull(guildId)
            return client.getLinkIfCached(guildId) ?: client.getOrCreateLink(guildId)
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        client = applicationContext.getBean(LavalinkClient::class.java)
    }
}
