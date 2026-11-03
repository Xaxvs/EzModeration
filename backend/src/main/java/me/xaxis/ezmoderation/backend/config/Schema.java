package me.xaxis.ezmoderation.backend.config;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface Schema {
  static String requireString(Object value) {
    if (!(value instanceof String string) || string.isBlank()) {
      throw new IllegalArgumentException("Expected a non-blank string");
    }
    return string;
  }

  static boolean requireBoolean(Object value) {
    if (!(value instanceof Boolean bool)) {
      throw new IllegalArgumentException("Expected a boolean");
    }
    return bool;
  }

  static UUID parseUuid(Object value) {
    try {
      return UUID.fromString(requireString(value));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Expected a valid UUID string");
    }
  }

  static Instant parseInstant(Object value) {
    if (!(value instanceof Byte
        || value instanceof Short
        || value instanceof Integer
        || value instanceof Long)) {
      throw new IllegalArgumentException("Expected an epoch millisecond value");
    }
    return Instant.ofEpochMilli(((Number) value).longValue());
  }

  default long requireLong(Object value) {
    if (!(value instanceof Number number)) {
      throw new IllegalArgumentException("Expected a number");
    }
    return number.longValue();
  }

  List<ConfigEntry> getEntries();
}
