package com.lm_pakkanen.radio_pele_java.util;

import java.util.Optional;
import org.springframework.lang.Nullable;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LavaLinkUtil {

  public static LavalinkPlayer getPlayer(@Nullable Link link) {

    if (link == null) {
      throw new IllegalStateException("Audio link not available.");
    }

    return Optional.ofNullable(link.getCachedPlayer())
        .orElse(link.createOrUpdatePlayer().block());
  }
}
