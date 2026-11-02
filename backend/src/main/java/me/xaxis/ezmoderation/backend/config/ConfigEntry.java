package me.xaxis.ezmoderation.backend.config;

public record ConfigEntry(String path, Type type) {

  public ConfigEntry {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("Config entry path cannot be blank");
    }
    if (type == null) {
      throw new IllegalArgumentException("Config entry type cannot be null");
    }
  }

  public enum Type {
    STRING,
    INTEGER,
    LONG,
    BOOLEAN,
    DOUBLE,
    LIST,
    UUID,
    ;
  }
}
