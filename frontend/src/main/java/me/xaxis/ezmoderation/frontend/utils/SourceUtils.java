package me.xaxis.ezmoderation.frontend.utils;

import java.util.UUID;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import org.bukkit.configuration.file.FileConfiguration;

public class SourceUtils {

  private SourceUtils() {}

  public static boolean isInstance(
      String path, ConfigEntry.Type expectedType, FileConfiguration yaml) {
    return switch (expectedType) {
      case UUID -> yaml.isString(path) && isUuid(yaml.getString(path));
      case STRING -> yaml.isString(path);
      case INTEGER -> yaml.isInt(path);
      case LONG -> yaml.isLong(path) || yaml.isInt(path);
      case BOOLEAN -> yaml.isBoolean(path);
      case DOUBLE -> yaml.isDouble(path) || yaml.isInt(path);
      case LIST -> yaml.isList(path);
    };
  }

  private static boolean isUuid(String value) {
    if (value == null) {
      return false;
    }
    try {
      UUID.fromString(value);
      return true;
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  public static boolean isValid(String path, FileConfiguration yaml) {
    return yaml.isSet(path);
  }
}
