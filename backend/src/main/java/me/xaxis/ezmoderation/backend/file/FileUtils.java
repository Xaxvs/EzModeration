package me.xaxis.ezmoderation.backend.file;

import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

public final class FileUtils {

  private FileUtils() {}

  public static void isolateFile(Path file) {
    if (file == null) {
      throw new NullPointerException("path cannot be null");
    }

    if (!Files.exists(file)) {
      return;
    }
    Path absoluteFile = file.toAbsolutePath();
    Path directory = absoluteFile.getParent();
    Path isolatedFile = getNextAvailable(directory, absoluteFile.getFileName().toString());
    try {
      try {
        Files.move(absoluteFile, isolatedFile, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException exception) {
        Files.move(absoluteFile, isolatedFile);
      }
    } catch (Exception exception) {
      throw new RuntimeException("Failed to isolate file: " + file.getFileName(), exception);
    }
  }

  private static Path getNextAvailable(Path directory, String baseName) {
    int index = 0;

    Path filePath = directory.resolve(baseName + "." + index + "." + "corrupted");
    while (Files.exists(filePath)) {
      index++;
      filePath = directory.resolve(baseName + "." + index + "." + "corrupted");
    }
    return filePath;
  }
}
