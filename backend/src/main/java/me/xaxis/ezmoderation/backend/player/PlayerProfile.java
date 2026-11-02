package me.xaxis.ezmoderation.backend.player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.infraction.Infraction;

public record PlayerProfile(
    UUID profileId,
    UUID playerId,
    PlayerActivity activity,
    PlayerInfractionHistory infractionHistory) {

  public PlayerProfile(
      UUID profileId,
      UUID playerId,
      List<Infraction> infractions,
      boolean isBanned,
      boolean isMuted,
      boolean isOnline,
      Instant lastLoginTime,
      Instant lastLogoutTime,
      String lastKnownName) {
    this(
        profileId,
        playerId,
        new PlayerActivity(isOnline, lastLoginTime, lastLogoutTime, lastKnownName),
        new PlayerInfractionHistory(infractions, isBanned, isMuted));
  }

  public PlayerProfile(UUID playerId, String playerName) {
    this(
        UUID.randomUUID(),
        playerId,
        new ArrayList<>(),
        false,
        false,
        false,
        Instant.EPOCH,
        Instant.EPOCH,
        playerName);
  }
}
