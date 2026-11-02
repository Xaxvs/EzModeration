package me.xaxis.ezmoderation.backend.player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.config.ConfigLoader;
import me.xaxis.ezmoderation.backend.infraction.Infraction;

public class PlayerProfileConfigLoader extends ConfigLoader<PlayerProfile> {

  private List<Infraction> parseInfractions(Object value) {
    if (!(value instanceof List<?> values)) {
      throw new IllegalArgumentException("Expected a list of infractions");
    }

    List<Infraction> infractions = new ArrayList<>();
    for (Object item : values) {
      if (!(item instanceof Map<?, ?> map)) {
        throw new IllegalArgumentException("Each infraction must be a map");
      }

      UUID playerId = parseUuid(map.get("player-id"));
      Infraction.Type type = Infraction.Type.valueOf(requireString(map.get("type")).toUpperCase());
      String reason = requireString(map.get("reason"));
      Instant timestamp = parseInstant(map.get("timestamp"));
      Object expiresAtValue = map.get("expires-at");
      Instant expiresAt = expiresAtValue == null ? null : parseInstant(expiresAtValue);
      infractions.add(Infraction.load(playerId, type, reason, timestamp, expiresAt));
    }
    return List.copyOf(infractions);
  }

  @Override
  public Optional<PlayerProfile> loadFromConfig(Map<String, Object> keys) {
    if (keys == null) {
      return Optional.empty();
    }

    try {
      UUID playerId = parseUuid(keys.get("player-id"));
      String playerName = requireString(keys.get("activity.last-known-name"));
      boolean online = requireBoolean(keys.get("activity.is-online"));
      Instant lastLogin = parseInstant(keys.get("activity.last-login-time"));
      Instant lastLogout = parseInstant(keys.get("activity.last-logout-time"));
      boolean banned = requireBoolean(keys.get("infraction-history.is-banned"));
      boolean muted = requireBoolean(keys.get("infraction-history.is-muted"));
      List<Infraction> infractions = parseInfractions(keys.get("infraction-history.infractions"));

      return Optional.of(
          new PlayerProfile(
              playerId, infractions, banned, muted, online, lastLogin, lastLogout, playerName));
    } catch (IllegalArgumentException | ClassCastException | NullPointerException exception) {
      return Optional.empty();
    }
  }
}
