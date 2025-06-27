package com.lm_pakkanen.radio_pele_java.util;

import java.util.Optional;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LavaLinkUtil implements ApplicationContextAware {

  private static LavalinkClient client;

  @Override
  public void setApplicationContext(
      @SuppressWarnings("null") ApplicationContext applicationContext) {
    LavaLinkUtil.client = applicationContext.getBean(LavalinkClient.class);
  }

  public static LavalinkPlayer getPlayer(@Nullable Long guildId) {
    final Link link = getLink(guildId);
    return Optional.ofNullable(link.getCachedPlayer())
        .orElse(link.createOrUpdatePlayer().block());
  }

  public static Link getLink(@Nullable Long guildId) {

    final long nonNullGuildId = Optional.ofNullable(guildId).orElseThrow(
        () -> new NullPointerException("Guild ID must not be null."));

    return Optional.ofNullable(client.getLinkIfCached(nonNullGuildId))
        .orElse(client.getOrCreateLink(nonNullGuildId));
  }
}
