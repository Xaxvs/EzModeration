package me.xaxis.ezmoderation.backend.config;

import java.util.Arrays;
import java.util.List;

public abstract class Config {

  private final List<ConfigEntry<?>> entries;

  public Config(ConfigEntry<?>... entries) {
    this.entries = Arrays.stream(entries).toList();
  }

  public List<ConfigEntry<?>> getEntries() {
    return entries;
  }
}
