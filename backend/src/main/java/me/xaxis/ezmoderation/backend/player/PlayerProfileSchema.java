package me.xaxis.ezmoderation.backend.player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.file.Schema;
import me.xaxis.ezmoderation.backend.infraction.Infraction;

public class PlayerProfileSchema implements Schema {

  public static Optional<PlayerProfile> load(Map<String, Object> values) {
    if (values == null) {
      return Optional.empty();
    }

    try {
      UUID playerId = Schema.parseUuid(values.get("player-id"));
      String playerName = Schema.requireString(values.get("activity.last-known-name"));
      boolean online = Schema.requireBoolean(values.get("activity.is-online"));
      Instant lastLogin = Schema.parseInstant(values.get("activity.last-login-time"));
      Instant lastLogout = Schema.parseInstant(values.get("activity.last-logout-time"));
      boolean banned = Schema.requireBoolean(values.get("infraction-history.is-banned"));
      boolean muted = Schema.requireBoolean(values.get("infraction-history.is-muted"));
      List<Infraction> infractions = parseInfractions(values.get("infraction-history.infractions"));

      return Optional.of(
          new PlayerProfile(
              playerId, infractions, banned, muted, online, lastLogin, lastLogout, playerName));
    } catch (IllegalArgumentException | ClassCastException | NullPointerException exception) {
      return Optional.empty();
    }
  }

  public static Map<String, Object> serialize(PlayerProfile playerProfile) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("player-id", playerProfile.getPlayerId().toString());
    values.put("activity.is-online", playerProfile.getActivity().isOnline());
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
    values.put("infraction-history.is-banned", playerProfile.getInfractionHistory().isBanned());
    values.put("infraction-history.is-muted", playerProfile.getInfractionHistory().isMuted());
    return Map.copyOf(values);
  }

  public static Map<String, Object> defaults(UUID playerId) {
    Map<String, Object> values = new LinkedHashMap<>();
    values.put("player-id", playerId.toString());
    values.put("activity.is-online", false);
    values.put("activity.last-login-time", 0L);
    values.put("activity.last-logout-time", 0L);
    values.put("activity.last-known-name", playerId.toString());
    values.put("infraction-history.infractions", List.of());
    values.put("infraction-history.is-banned", false);
    values.put("infraction-history.is-muted", false);
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
          Infraction.Type.valueOf(Schema.requireString(map.get("type")).toUpperCase());
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
        new ConfigEntry("activity.is-online", ConfigEntry.Type.BOOLEAN),
        new ConfigEntry("activity.last-login-time", ConfigEntry.Type.LONG),
        new ConfigEntry("activity.last-logout-time", ConfigEntry.Type.LONG),
        new ConfigEntry("activity.last-known-name", ConfigEntry.Type.STRING),
        new ConfigEntry("infraction-history.infractions", ConfigEntry.Type.LIST),
        new ConfigEntry("infraction-history.is-banned", ConfigEntry.Type.BOOLEAN),
        new ConfigEntry("infraction-history.is-muted", ConfigEntry.Type.BOOLEAN));
  }
}
