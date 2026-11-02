package me.xaxis.ezmoderation.backend.player;

import me.xaxis.ezmoderation.backend.config.Config;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;

import java.util.List;

public class PlayerProfileSchema extends Config {

  public PlayerProfileSchema() {
    super(
        new ConfigEntry<>("profileId", String.class),
        new ConfigEntry<>("playerId", String.class),
        new ConfigEntry<>("activity.isOnline", Boolean.class),
        new ConfigEntry<>("activity.lastLoginTime", Long.class),
        new ConfigEntry<>("activity.lastLogoutTime", Long.class),
        new ConfigEntry<>("activity.lastKnownName", String.class),
        new ConfigEntry<>("infractionHistory.infractions", List.class),
        new ConfigEntry<>("infractionHistory.isBanned", Boolean.class),
        new ConfigEntry<>("infractionHistory.isMuted", Boolean.class));
  }

}
