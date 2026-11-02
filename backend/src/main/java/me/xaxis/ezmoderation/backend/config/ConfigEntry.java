package me.xaxis.ezmoderation.backend.config;

public record ConfigEntry<T>(String path, Type type) {

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
