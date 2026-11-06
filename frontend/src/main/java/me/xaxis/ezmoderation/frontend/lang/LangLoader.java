package me.xaxis.ezmoderation.frontend.lang;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.ConfigEntry;
import me.xaxis.ezmoderation.backend.config.ConfigFile;
import me.xaxis.ezmoderation.backend.config.ConfigValidator;
import me.xaxis.ezmoderation.backend.config.Loader;
import me.xaxis.ezmoderation.backend.lang.Lang;
import me.xaxis.ezmoderation.backend.lang.LangSchema;
import me.xaxis.ezmoderation.frontend.utils.SourceUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LangLoader implements Loader<Lang> {

  private final Logger logger;
  private final ConfigFile configFile;
  private final FileConfiguration yaml;
  private boolean writeDefaults;

  public LangLoader(Path path, Logger logger) {
    this.configFile = new ConfigFile(path, "lang.yml");
    this.writeDefaults = configFile.initialize();
    this.logger = logger;
    this.yaml = YamlConfiguration.loadConfiguration(configFile.getPath().toFile());
  }

  @Override
  public Lang load() {
    if (writeDefaults) {
      write(LangSchema.defaults());
      writeDefaults = false;
    }

    ConfigValidator configValidator = new ConfigValidator(this, logger);
    if (!configValidator.validate(yaml.getValues(true), new LangSchema())) {
      return null;
    }

    return LangSchema.load(yaml.getValues(true));
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
  public void save() {
    configFile.save(yaml.saveToString());
  }

  @Override
  public boolean isValid(String path) {
    return SourceUtils.isValid(path, yaml);
  }
}
