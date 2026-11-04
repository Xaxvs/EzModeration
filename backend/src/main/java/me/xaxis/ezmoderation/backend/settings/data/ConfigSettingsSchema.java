package me.xaxis.ezmoderation.backend.settings.data;

import java.util.List;
import java.util.Map;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.Schema;
import me.xaxis.ezmoderation.backend.settings.ConfigSettings;

public class ConfigSettingsSchema implements Schema {

  public static Map<String, Object> defaults() {
    return Map.of();
  }

  public static Map<String, Object> serialize(ConfigSettings settings) {
    // todo
    return null;
  }

  public static ConfigSettings load(Map<String, Object> values) {
    if (values == null) {
      return null;
    }
    // todo
    return new ConfigSettings();
  }

  @Override
  public List<ConfigEntry> getEntries() {
    return List.of();
  }
}
