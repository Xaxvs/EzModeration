package me.xaxis.ezmoderation.backend.player;

import me.xaxis.ezmoderation.backend.config.Config;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;

public class PlayerProfileSchema extends Config {

  public PlayerProfileSchema() {
    super(
        new ConfigEntry<>("profile-id", ConfigEntry.Type.STRING),
        new ConfigEntry<>("player-id", ConfigEntry.Type.STRING),
        new ConfigEntry<>("activity.is-online", ConfigEntry.Type.BOOLEAN),
        new ConfigEntry<>("activity.last-login-time", ConfigEntry.Type.LONG),
        new ConfigEntry<>("activity.last-logout-time", ConfigEntry.Type.LONG),
        new ConfigEntry<>("activity.last-known-name", ConfigEntry.Type.STRING),
        new ConfigEntry<>("infraction-history.infractions", ConfigEntry.Type.LIST),
        new ConfigEntry<>("infraction-history.is-banned", ConfigEntry.Type.BOOLEAN),
        new ConfigEntry<>("infraction-history.is-muted", ConfigEntry.Type.BOOLEAN));
  }
}
