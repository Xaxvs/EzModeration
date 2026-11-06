package me.xaxis.ezmoderation.backend.lang;

import java.util.List;
import java.util.Map;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.Schema;

public class LangSchema implements Schema {

  public static Map<String, Object> defaults() {
    return Map.of(
        "no-permissions", "&cYou do not have permission to execute this command.",
        "sender-cant-execute-command", "&cYou cannot execute this command.",
        "player-not-found", "&cPlayer: &7{player} &cnot found.",
        "history-command-usage", "&cUsage: /history <player>");
  }

  public static Lang load(Map<String, Object> values) {
    if (values == null) {
      return null;
    }

    return new Lang(
        Schema.requireString(values.get("no-permissions")),
        Schema.requireString(values.get("sender-cant-execute-command")),
        Schema.requireString(values.get("player-not-found")),
        Schema.requireString(values.get("history-command-usage")));
  }

  @Override
  public List<ConfigEntry> getEntries() {
    return List.of(
        new ConfigEntry("no-permissions", ConfigEntry.Type.STRING),
        new ConfigEntry("sender-cant-execute-command", ConfigEntry.Type.STRING),
        new ConfigEntry("player-not-found", ConfigEntry.Type.STRING),
        new ConfigEntry("history-command-usage", ConfigEntry.Type.STRING));
  }
}
