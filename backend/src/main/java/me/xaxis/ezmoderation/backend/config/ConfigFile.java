package me.xaxis.ezmoderation.backend.config;

import java.nio.file.Files;
import java.nio.file.Path;
import me.xaxis.ezmoderation.backend.file.SafeFileWrite;

public class ConfigFile {

  private final Path path;

  public ConfigFile(Path folder, String fileName) {
    if (folder == null || fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("Folder and fileName cannot be null or blank");
    }
    this.path = folder.resolve(fileName);
  }

  public void save(String content) {
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Content cannot be null or blank");
    }
    try {
      SafeFileWrite.save(path, content);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save config file", e);
    }
  }

  public void delete() {
    try {
      Files.deleteIfExists(path);
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete config file", e);
    }
  }

  public Path getPath() {
    return path;
  }

  public boolean initialize() {

    boolean shouldWriteDefaults = false;
    Path parentDir = path.getParent();
    if (parentDir != null && !Files.exists(parentDir)) {
      try {
        Files.createDirectories(parentDir);
      } catch (Exception e) {
        throw new RuntimeException(
            "Failed to create directories", e);
      }
    }

    if (!Files.exists(path)) {
      try {
        Files.createFile(path);
        shouldWriteDefaults = true;
      } catch (Exception e) {
        throw new RuntimeException("Failed to create config file", e);
      }
    }
    return shouldWriteDefaults;
  }
}
