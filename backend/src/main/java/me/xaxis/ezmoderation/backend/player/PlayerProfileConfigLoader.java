package me.xaxis.ezmoderation.backend.player;

import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.infraction.Infraction;

public class PlayerProfileConfigLoader {

  public static Optional<PlayerProfile> loadPlayerProfileFromConfig(Map<String, Object> keys) {
    if (keys == null) {
      return Optional.empty();
    }

    try {
      UUID profileId = parseUuid(keys.get("profile-id"));
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
              profileId,
              playerId,
              infractions,
              banned,
              muted,
              online,
              lastLogin,
              lastLogout,
              playerName));
    } catch (IllegalArgumentException | ClassCastException | NullPointerException exception) {
      return Optional.empty();
    }
  }

  private static String requireString(Object value) {
    if (!(value instanceof String string) || string.isBlank()) {
      throw new IllegalArgumentException("Expected a non-blank string");
    }
    return string;
  }

  private static boolean requireBoolean(Object value) {
    if (!(value instanceof Boolean bool)) {
      throw new IllegalArgumentException("Expected a boolean");
    }
    return bool;
  }

  private static UUID parseUuid(Object value) {
    return UUID.fromString(requireString(value));
  }

  private static Instant parseInstant(Object value) {
    if (!(value instanceof Number number)) {
      throw new IllegalArgumentException("Expected an epoch millisecond value");
    }
    return Instant.ofEpochMilli(number.longValue());
  }

  private static List<Infraction> parseInfractions(Object value) {
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
}
