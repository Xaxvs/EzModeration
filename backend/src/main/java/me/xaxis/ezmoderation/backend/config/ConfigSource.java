package me.xaxis.ezmoderation.backend.config;

public interface ConfigSource {

  boolean isInstance(String path, ConfigEntry.Type expectedType);

  boolean isValid(String path);
}
