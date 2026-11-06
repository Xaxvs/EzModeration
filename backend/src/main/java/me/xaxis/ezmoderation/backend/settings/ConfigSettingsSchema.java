package me.xaxis.ezmoderation.backend.settings;

import java.util.List;
import java.util.Map;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.Schema;

public class ConfigSettingsSchema implements Schema {

  public static Map<String, Object> defaults() {
    return Map.of("profile-auto-save-delay", 20 * 30L);
  }

  public static ConfigSettings load(Map<String, Object> values) {
    if (values == null) {
      return null;
    }
    return new ConfigSettings((long) values.getOrDefault("profile-auto-save-delay", 20 * 30L));
  }

  @Override
  public List<ConfigEntry> getEntries() {
    return List.of(new ConfigEntry("profile-auto-save-delay", ConfigEntry.Type.LONG));
  }
}
