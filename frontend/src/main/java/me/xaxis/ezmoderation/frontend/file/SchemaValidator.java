package me.xaxis.ezmoderation.frontend.file;

import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.Config;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import org.bukkit.configuration.file.FileConfiguration;

public class SchemaValidator {

  private static boolean checkObjectType(Object object, Class<?> expectedType, String path, Logger logger) {
    if (object == null) {
      logger.warning("Missing config entry: " + path);
      return false;
    }

    if (!expectedType.isInstance(object)) {
      logger.warning(
          "Config entry type mismatch for path: "
              + path
              + ". Expected: "
              + expectedType
              + ", but got: "
              + object.getClass().getName());
      return false;
    }
    return true;
  }

  public static boolean validate(FileConfiguration yaml, Logger logger, Config config) {
    boolean isValid = true;
    for (int i = 0; i < config.getEntries().size(); i++) {
      ConfigEntry<?> entry = config.getEntries().get(i);
      Object value = yaml.get(entry.path());
      isValid = isValid && checkObjectType(value, entry.value(), entry.path(), logger);
    }
    return isValid;
  }
}
