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

  private final FileConfiguration yaml;
  private final Logger logger;
  private final UUID playerId;
  private final ConfigFile configFile;
  private boolean writeDefaults;

  PlayerProfileLoader(Path path, Logger logger, UUID playerId) {
    this.configFile = new ConfigFile(path, playerId + ".yml");
    this.writeDefaults = configFile.initialize();
    this.yaml = YamlConfiguration.loadConfiguration(configFile.getPath().toFile());
    this.logger = logger;
    this.playerId = playerId;
  }

  public boolean shouldSave() {
    return writeDefaults;
  }

  @Override
  public PlayerProfile load() {

    if (writeDefaults) {
      write(PlayerProfileSchema.defaults(playerId));
      writeDefaults = false;
    }

    ConfigValidator configValidator = new ConfigValidator(this, logger);
    if (!configValidator.validate(yaml.getValues(true), new PlayerProfileSchema())) {
      return recoverCorruptProfile();
    }

    return PlayerProfileSchema.load(yaml.getValues(true)).orElseGet(this::recoverCorruptProfile);
  }

  private PlayerProfile recoverCorruptProfile() {
    FileUtils.isolateFile(configFile.getPath());
    logger.severe(
        "Failed to load player profile. The file has been isolated. "
            + "Please check the logs for more information. "
            + "A new profile will be created for the player.");
    writeDefaults = true;
    return load();
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
  }
}
