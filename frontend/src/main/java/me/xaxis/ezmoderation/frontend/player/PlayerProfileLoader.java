package me.xaxis.ezmoderation.frontend.player;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.ConfigValidator;
import me.xaxis.ezmoderation.backend.config.ConfigSource;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.PlayerProfileConfigLoader;
import me.xaxis.ezmoderation.backend.player.PlayerProfileSchema;
import org.bukkit.configuration.file.FileConfiguration;

public class PlayerProfileLoader implements ConfigSource {

  private final FileConfiguration yaml;
  private final Logger logger;

  public PlayerProfileLoader(FileConfiguration yaml, Logger logger) {
    this.yaml = yaml;
    this.logger = logger;
  }

  public PlayerProfile loadPlayerProfile() {

    ConfigValidator configValidator = new ConfigValidator(this, logger);
    if (!configValidator.validate(yaml.getValues(true), new PlayerProfileSchema())) {
      logger.severe("Failed to validate player profile.");
      return null;
    }

    PlayerProfileConfigLoader playerProfileConfigLoader = new PlayerProfileConfigLoader();

    Optional<PlayerProfile> optionalPlayerProfile =
        playerProfileConfigLoader.loadFromConfig(yaml.getValues(true));

    if (optionalPlayerProfile.isPresent()) {
      return optionalPlayerProfile.get();
    } else {
      logger.severe("Failed to load player profile.");
      // todo: handle the case where the player profile could not be loaded

      return null;
    }
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

  @Override
  public boolean isInstance(String path, ConfigEntry.Type expectedType) {
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

  @Override
  public boolean isValid(String path) {
    return yaml.isSet(path);
  }
}
