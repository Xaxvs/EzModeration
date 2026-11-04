package me.xaxis.ezmoderation.backend.config;

import java.util.Map;
import java.util.logging.Logger;

public record ConfigValidator(Loader loader, Logger logger) {

  public ConfigValidator {
    if (loader == null) {
      throw new NullPointerException("Config source cannot be null");
    }
    if (logger == null) {
      throw new NullPointerException("Logger cannot be null");
    }
  }

  private boolean checkObjectType(ConfigEntry.Type expectedType, Object object, String path) {
    if (object == null || !loader.isInstance(path, expectedType)) {
      logger.warning(
          "Config entry type mismatch for path: "
              + path
              + ". Expected: "
              + expectedType
              + ", but got: "
              + (object == null ? "null" : object.getClass().getName()));
      return false;
    }
    return true;
  }

  public boolean validate(Map<String, Object> keys, Schema schema) {
    if (keys == null || schema == null) {
      throw new IllegalArgumentException("Keys and schema must not be null");
    }
    boolean isValid = true;
    for (int i = 0; i < schema.getEntries().size(); i++) {
      ConfigEntry entry = schema.getEntries().get(i);
      ConfigEntry.Type expectedType = entry.type();
      if (!loader.isValid(entry.path())) {
        logger.warning("Missing config entry: " + entry.path());
        isValid = false;
        continue;
      }
      if (!checkObjectType(expectedType, keys.get(entry.path()), entry.path())) {
        isValid = false;
      }
    }
    return isValid;
  }
}
