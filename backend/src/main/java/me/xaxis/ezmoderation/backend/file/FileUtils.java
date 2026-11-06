package me.xaxis.ezmoderation.backend.file;

import java.io.File;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FileUtils {

  private FileUtils() {}

  public static File[] getFiles(Path folderPath) {
    if (folderPath == null) {
      throw new IllegalArgumentException("folderPath cannot be null");
    }
    File folder = folderPath.toFile();
    if (!folder.exists()) {
      throw new IllegalArgumentException("Folder " + folderPath + " does not exist");
    }
    if (!folder.isDirectory()) {
      throw new IllegalArgumentException("Folder " + folderPath + " must be a directory");
    }
    File[] files = folder.listFiles();
    if (files == null) {
      throw new IllegalArgumentException("Folder " + folderPath + " is not accessible");
    }
    return files;
  }

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
