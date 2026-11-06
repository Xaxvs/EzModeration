package me.xaxis.ezmoderation.backend.settings;

public record ConfigSettings(long profileAutoSaveDelay) {
  public ConfigSettings {
    if (profileAutoSaveDelay < 0) {
      throw new IllegalArgumentException("profileAutoSaveDelay cannot be negative");
    }
  }
}
