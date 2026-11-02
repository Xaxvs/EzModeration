package me.xaxis.ezmoderation.frontend.file;

import java.util.Optional;
import java.util.logging.Logger;

import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.PathValidator;
import me.xaxis.ezmoderation.backend.file.Validator;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.PlayerProfileConfigLoader;
import me.xaxis.ezmoderation.backend.player.PlayerProfileSchema;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLoader implements PathValidator {

  private final FileConfiguration yaml;
  private final Logger logger;

  public ConfigLoader(FileConfiguration yaml, Logger logger) {
    this.yaml = yaml;
    this.logger = logger;
  }

  public PlayerProfile loadPlayerProfile() {

    Validator validator = new Validator(this, logger);
    if (!validator.validate(yaml.getValues(true), new PlayerProfileSchema())) {
      logger.severe("Failed to validate player profile.");
      return null;
    }

    Optional<PlayerProfile> optionalPlayerProfile =
        PlayerProfileConfigLoader.loadPlayerProfileFromConfig(yaml.getValues(true));
    if (optionalPlayerProfile.isPresent()) {
      return optionalPlayerProfile.get();
    } else {
      logger.severe("Failed to load player profile.");
      // todo: handle the case where the player profile could not be loaded

      return null;
    }
  }

  @Override
  public boolean isInstance(String path, ConfigEntry.Type expectedType) {
    return switch (expectedType) {
      case UUID -> yaml.isString(path) && Validator.validateUUID(yaml.getString(path)) != null;
      case STRING -> yaml.isString(path);
      case INTEGER -> yaml.isInt(path);
      case LONG -> yaml.isLong(path) || yaml.isInt(path);
      case BOOLEAN -> yaml.isBoolean(path);
      case DOUBLE -> yaml.isDouble(path) || yaml.isInt(path);
      case LIST -> yaml.isList(path);
    };
  }
}
