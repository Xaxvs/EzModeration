package me.xaxis.ezmoderation.backend.file;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.Config;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.PathValidator;

public class Validator {

  private final PathValidator pathValidator;
  private final Logger logger;

  public Validator(PathValidator pathValidator, Logger logger) {
    this.pathValidator = Objects.requireNonNull(pathValidator, "pathValidator");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  public static UUID validateUUID(String uuidString) {
    if (uuidString == null) {
      return null;
    }
    try {
      return UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private boolean checkObjectType(ConfigEntry.Type expectedType, Object object, String path) {
    if (object == null || !pathValidator.isInstance(path, expectedType)) {
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

  public boolean validate(Map<String, Object> keys, Config config) {
    Objects.requireNonNull(keys, "keys");
    Objects.requireNonNull(config, "config");
    boolean isValid = true;
    for (int i = 0; i < config.getEntries().size(); i++) {
      ConfigEntry<?> entry = config.getEntries().get(i);
      ConfigEntry.Type expectedType = entry.type();
      if (!keys.containsKey(entry.path())) {
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
