package me.xaxis.ezmoderation.backend.lang;

import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.Schema;

import java.util.List;
import java.util.Map;

public class LangSchema implements Schema {

  public static Map<String, Object> defaults() {
    return Map.of(
        "no-permissions", "You do not have permission to execute this command.",
        "sender-cant-execute-command", "You cannot execute this command.",
        "player-not-found", "Player: {player} not found."
    );
  }

  public static Lang load(Map<String, Object> values) {
    if (values == null) {
      return null;
    }
    return new Lang(
        (String) values.getOrDefault("no-permissions", "You do not have permission to execute this command."),
        (String) values.getOrDefault("sender-cant-execute-command", "You cannot execute this command."),
        (String) values.getOrDefault("player-not-found", "Player: {player} not found.")
    );
  }

  @Override
  public List<ConfigEntry> getEntries() {
    return List.of(
        new ConfigEntry("no-permissions", ConfigEntry.Type.STRING),
        new ConfigEntry("sender-cant-execute-command", ConfigEntry.Type.STRING),
        new ConfigEntry("player-not-found", ConfigEntry.Type.STRING)
    );
  }
}
