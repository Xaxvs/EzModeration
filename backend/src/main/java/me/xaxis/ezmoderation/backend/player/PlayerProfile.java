package me.xaxis.ezmoderation.backend.player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.infraction.Infraction;

public class PlayerProfile {

  private final UUID playerId;
  private final PlayerActivity activity;
  private final PlayerInfractionHistory infractionHistory;

  public PlayerProfile(
      UUID playerId,
      List<Infraction> infractions,
      boolean isBanned,
      boolean isMuted,
      boolean isOnline,
      Instant lastLoginTime,
      Instant lastLogoutTime,
      String lastKnownName) {
    if (playerId == null) {
      throw new IllegalArgumentException("Player ID is required");
    }
    this.playerId = playerId;
    this.activity = new PlayerActivity(isOnline, lastLoginTime, lastLogoutTime, lastKnownName);
    this.infractionHistory = new PlayerInfractionHistory(infractions, isBanned, isMuted);
  }

  public PlayerProfile(UUID playerId, String playerName) {
    this(
        playerId, new ArrayList<>(), false, false, false, Instant.EPOCH, Instant.EPOCH, playerName);
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public PlayerActivity getActivity() {
    return activity;
  }

  public PlayerInfractionHistory getInfractionHistory() {
    return infractionHistory;
  }
}
