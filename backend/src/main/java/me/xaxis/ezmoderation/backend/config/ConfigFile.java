package me.xaxis.ezmoderation.backend.config;

import java.nio.file.Files;
import java.nio.file.Path;
import me.xaxis.ezmoderation.backend.file.SafeFileWrite;

public class ConfigFile {

  private final Path path;

  public ConfigFile(Path folder, String fileName) {
    this.path = folder.resolve(fileName);
  }

  public void save(String content) {
    try {
      SafeFileWrite.save(path, content);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save player profile config file", e);
    }
  }

  public void delete() {
    try {
      Files.deleteIfExists(path);
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete player profile config file", e);
    }
  }

  public void initialize() {

    Path parentDir = path.getParent();

    if (parentDir != null && !Files.exists(parentDir)) {
      try {
        Files.createDirectories(parentDir);
      } catch (Exception e) {
        throw new RuntimeException(
            "Failed to create directories for player profile config file", e);
      }
    }

    if (!Files.exists(path)) {
      try {
        Files.createFile(path);
      } catch (Exception e) {
        throw new RuntimeException("Failed to create player profile config file", e);
      }
    }
  }
}
