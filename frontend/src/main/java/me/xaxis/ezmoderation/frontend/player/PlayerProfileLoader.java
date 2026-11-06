package me.xaxis.ezmoderation.frontend.player;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.ConfigFile;
import me.xaxis.ezmoderation.backend.config.ConfigValidator;
import me.xaxis.ezmoderation.backend.config.Loader;
import me.xaxis.ezmoderation.backend.file.FileUtils;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.data.PlayerProfileSchema;
import me.xaxis.ezmoderation.frontend.utils.SourceUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

final class PlayerProfileLoader implements Loader<PlayerProfile> {

  private final Logger logger;
  private final UUID playerId;
  private final ConfigFile configFile;
  private FileConfiguration yaml;
  private boolean writeDefaults;
  private boolean requiresSave;

  PlayerProfileLoader(Path path, Logger logger, UUID playerId) {
    this.configFile = new ConfigFile(path, playerId + ".yml");
    this.writeDefaults = configFile.initialize();
    this.requiresSave = writeDefaults;
    this.yaml = YamlConfiguration.loadConfiguration(configFile.getPath().toFile());
    this.logger = logger;
    this.playerId = playerId;
  }

  @Override
  public PlayerProfile load() {

    if (writeDefaults) {
      PlayerProfile profile =
          PlayerProfileSchema.createFrom(PlayerProfileSchema.defaults(playerId))
              .orElseThrow(() ->
                  new IllegalStateException(
                      "Failed to load default profile for player: " + playerId));
      writeDefaults = false;
      return profile;
    }

    ConfigValidator configValidator = new ConfigValidator(this, logger);
    if (!configValidator.validate(yaml.getValues(true), new PlayerProfileSchema())) {
      return isolateAndCreateProfile();
    }

    return PlayerProfileSchema.createFrom(yaml.getValues(true))
        .orElseGet(this::isolateAndCreateProfile);
  }

  private PlayerProfile isolateAndCreateProfile() {
    FileUtils.isolateFile(configFile.getPath());
    configFile.initialize();
    yaml = YamlConfiguration.loadConfiguration(configFile.getPath().toFile());
    writeDefaults = true;
    requiresSave = true;
    logger.severe(
        configFile.getPath().toString()
            + " is corrupt and cannot be loaded. "
            + "The file has been isolated. Please check the logs for more information. "
            + "A new profile will be created for the player.");
    return null;
  }

  boolean requiresSave() {
    return requiresSave;
  }

  @Override
  public boolean isInstance(String path, ConfigEntry.Type expectedType) {
    return SourceUtils.isInstance(path, expectedType, yaml);
  }

  @Override
  public boolean isValid(String path) {
    return SourceUtils.isValid(path, yaml);
  }

  @Override
  public void write(Map<String, Object> values) {
    values.forEach(yaml::set);
  }

  @Override
  public void save() {
    configFile.save(yaml.saveToString());
    writeDefaults = false;
    requiresSave = false;
  }

  void save(PlayerProfile profile) {
    if (profile == null) {
      throw new IllegalArgumentException("profile cannot be null");
    }
    write(PlayerProfileSchema.serialize(profile));
    save();
  }
}
