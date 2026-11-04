package me.xaxis.ezmoderation.frontend.settings;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.ConfigFile;
import me.xaxis.ezmoderation.backend.config.ConfigValidator;
import me.xaxis.ezmoderation.backend.config.Loader;
import me.xaxis.ezmoderation.backend.settings.ConfigSettings;
import me.xaxis.ezmoderation.backend.settings.data.ConfigSettingsSchema;
import me.xaxis.ezmoderation.frontend.utils.SourceUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.Nullable;

public class ConfigSettingsLoader implements Loader<ConfigSettings> {

  private final Logger logger;
  private final ConfigFile configFile;
  private final FileConfiguration yaml;
  private boolean writeDefaults;

  public ConfigSettingsLoader(Path path, Logger logger) {
    this.configFile = new ConfigFile(path, "config.yml");
    this.writeDefaults = configFile.initialize();
    this.logger = logger;
    this.yaml = YamlConfiguration.loadConfiguration(configFile.getPath().toFile());
  }

  public @Nullable ConfigSettings load() {
    if (writeDefaults) {
      write(ConfigSettingsSchema.defaults());
      writeDefaults = false;
    }

    ConfigValidator configValidator = new ConfigValidator(this, logger);
    if (!configValidator.validate(yaml.getValues(true), new ConfigSettingsSchema())) {
      return null;
    }

    return ConfigSettingsSchema.load(yaml.getValues(true));
  }

  @Override
  public void write(Map<String, Object> values) {
    values.forEach(yaml::set);
  }

  @Override
  public boolean isInstance(String path, ConfigEntry.Type expectedType) {
    return SourceUtils.isInstance(path, expectedType, yaml);
  }

  @Override
  public boolean isValid(String path) {
    return SourceUtils.isValid(path, yaml);
  }

  public void save() {
    configFile.save(yaml.saveToString());
  }
}
