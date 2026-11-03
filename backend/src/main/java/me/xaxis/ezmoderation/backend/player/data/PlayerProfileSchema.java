package me.xaxis.ezmoderation.backend.player.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Locale;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.Schema;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.infraction.Infraction;

public class PlayerProfileSchema implements Schema {

  public static Optional<PlayerProfile> load(Map<String, Object> values) {
    if (values == null) {
      return Optional.empty();
    }

    try {
      UUID playerId = Schema.parseUuid(values.get("player-id"));
      String playerName = Schema.requireString(values.get("activity.last-known-name"));
      Instant lastLogin = Schema.parseInstant(values.get("activity.last-login-time"));
      Instant lastLogout = Schema.parseInstant(values.get("activity.last-logout-time"));
      List<Infraction> infractions = parseInfractions(values.get("infraction-history.infractions"));

      return Optional.of(
          new PlayerProfile(
              playerId, infractions, false, lastLogin, lastLogout, playerName));
    } catch (IllegalArgumentException | ClassCastException | NullPointerException exception) {
      return Optional.empty();
    }
  }

  public static Map<String, Object> serialize(PlayerProfile playerProfile) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("player-id", playerProfile.getPlayerId().toString());
    values.put(
        "activity.last-login-time", playerProfile.getActivity().getLastLogin().toEpochMilli());
    values.put(
        "activity.last-logout-time", playerProfile.getActivity().getLastLogout().toEpochMilli());
    values.put("activity.last-known-name", playerProfile.getActivity().getLastKnownName());
    values.put(
        "infraction-history.infractions",
        playerProfile.getInfractionHistory().getInfractions().stream()
            .map(PlayerProfileSchema::serializeInfraction)
            .toList());
    return Map.copyOf(values);
  }

  public static Map<String, Object> defaults(UUID playerId) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("player-id", playerId.toString());
    values.put("activity.last-login-time", 0L);
    values.put("activity.last-logout-time", 0L);
    values.put("activity.last-known-name", playerId.toString());
    values.put("infraction-history.infractions", List.of());
    return Map.copyOf(values);
  }

  private static Map<String, Object> serializeInfraction(Infraction infraction) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("player-id", infraction.getPlayerId().toString());
    values.put("type", infraction.getType().name());
    values.put("reason", infraction.getReason());
    values.put("timestamp", infraction.getTimestamp().toEpochMilli());
    infraction.expiresAt().ifPresent(expiry -> values.put("expires-at", expiry.toEpochMilli()));
    return Map.copyOf(values);
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

      UUID playerId = Schema.parseUuid(map.get("player-id"));
      Infraction.Type type =
          Infraction.Type.valueOf(
              Schema.requireString(map.get("type")).toUpperCase(Locale.ROOT));
      String reason = Schema.requireString(map.get("reason"));
      Instant timestamp = Schema.parseInstant(map.get("timestamp"));
      Object expiresAtValue = map.get("expires-at");
      Instant expiresAt = expiresAtValue == null ? null : Schema.parseInstant(expiresAtValue);
      infractions.add(Infraction.load(playerId, type, reason, timestamp, expiresAt));
    }
    return List.copyOf(infractions);
  }

  @Override
  public List<ConfigEntry> getEntries() {
    return List.of(
        new ConfigEntry("player-id", ConfigEntry.Type.UUID),
        new ConfigEntry("activity.last-login-time", ConfigEntry.Type.LONG),
        new ConfigEntry("activity.last-logout-time", ConfigEntry.Type.LONG),
        new ConfigEntry("activity.last-known-name", ConfigEntry.Type.STRING),
        new ConfigEntry("infraction-history.infractions", ConfigEntry.Type.LIST));
  }
}
