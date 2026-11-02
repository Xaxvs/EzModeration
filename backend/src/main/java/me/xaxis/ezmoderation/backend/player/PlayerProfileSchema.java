package me.xaxis.ezmoderation.backend.player;

import me.xaxis.ezmoderation.backend.config.Config;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;

public class PlayerProfileSchema extends Config {

  public PlayerProfileSchema() {
    super(
        new ConfigEntry<>("profileId", ConfigEntry.Type.STRING),
        new ConfigEntry<>("playerId", ConfigEntry.Type.STRING),
        new ConfigEntry<>("activity.isOnline", ConfigEntry.Type.BOOLEAN),
        new ConfigEntry<>("activity.lastLoginTime", ConfigEntry.Type.LONG),
        new ConfigEntry<>("activity.lastLogoutTime", ConfigEntry.Type.LONG),
        new ConfigEntry<>("activity.lastKnownName", ConfigEntry.Type.STRING),
        new ConfigEntry<>("infractionHistory.infractions", ConfigEntry.Type.LIST),
        new ConfigEntry<>("infractionHistory.isBanned", ConfigEntry.Type.BOOLEAN),
        new ConfigEntry<>("infractionHistory.isMuted", ConfigEntry.Type.BOOLEAN));
  }
}
