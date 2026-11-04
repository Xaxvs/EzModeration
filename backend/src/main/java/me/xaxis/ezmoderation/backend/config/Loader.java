package me.xaxis.ezmoderation.backend.config;

import java.util.Map;

public interface Loader<T> {

  T load();

  void write(Map<String, Object> values);

  boolean isInstance(String path, ConfigEntry.Type expectedType);

  void save();

  boolean isValid(String path);
}
