package me.xaxis.ezmoderation.backend.config;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class ConfigLoader<T> {

  protected String requireString(Object value) {
    if (!(value instanceof String string) || string.isBlank()) {
      throw new IllegalArgumentException("Expected a non-blank string");
    }
    return string;
  }

  protected boolean requireBoolean(Object value) {
    if (!(value instanceof Boolean bool)) {
      throw new IllegalArgumentException("Expected a boolean");
    }
    return bool;
  }

  protected UUID parseUuid(Object value) {
    return UUID.fromString(requireString(value));
  }

  protected Instant parseInstant(Object value) {
    if (!(value instanceof Number number)) {
      throw new IllegalArgumentException("Expected an epoch millisecond value");
    }
    return Instant.ofEpochMilli(number.longValue());
  }

  public abstract Optional<T> loadFromConfig(Map<String, Object> values);
}
