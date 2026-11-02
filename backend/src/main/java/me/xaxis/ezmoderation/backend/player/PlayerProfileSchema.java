package me.xaxis.ezmoderation.backend.player;

import java.util.List;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.Schema;

public class PlayerProfileSchema implements Schema {

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
