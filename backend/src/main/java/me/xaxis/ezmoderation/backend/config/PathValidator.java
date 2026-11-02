package me.xaxis.ezmoderation.backend.config;

public interface PathValidator {

  boolean isInstance(String path, ConfigEntry.Type expectedType);

}
