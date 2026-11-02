package me.xaxis.ezmoderation.backend.player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerProfileConfigLoader {

  public static Optional<PlayerProfile> loadPlayerProfileFromConfig(Map<String, Object> keys) {

    UUID playerId = UUID.fromString((String) keys.get("player-id"));
    String playerName = (String) keys.get("player-name");

    // todo: add more validation for other fields
    return Optional.empty();
  }
}
