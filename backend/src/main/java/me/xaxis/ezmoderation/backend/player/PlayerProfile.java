package me.xaxis.ezmoderation.backend.player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.player.infraction.Infraction;

public record PlayerProfile(UUID playerId, PlayerActivity activity, PlayerInfractionHistory infractionHistory) {

  public PlayerProfile(
      UUID playerId,
      List<Infraction> infractions,
      boolean isOnline,
      Instant lastLoginTime,
      Instant lastLogoutTime,
      String lastKnownName) {
    if (playerId == null) {
      throw new IllegalArgumentException("Player ID is required");
    }
    if (infractions == null
        || infractions.stream()
            .anyMatch(
                infraction ->
                    infraction == null || !playerId.equals(infraction.getPlayerId()))) {
      throw new IllegalArgumentException("Infractions must belong to the player profile");
    }
    this(playerId, new PlayerActivity(isOnline, lastLoginTime, lastLogoutTime, lastKnownName), new PlayerInfractionHistory(infractions));
  }

  public PlayerProfile(UUID playerId, String playerName) {
    this(playerId, new ArrayList<>(), false, Instant.EPOCH, Instant.EPOCH, playerName);
  }
}
