package me.xaxis.ezmoderation.backend.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Config {

  private final List<ConfigEntry<?>> entries;

  public Config(ConfigEntry<?>... entries) {
    Objects.requireNonNull(entries, "entries");
    List<ConfigEntry<?>> validatedEntries = new ArrayList<>(entries.length);
    for (ConfigEntry<?> entry : entries) {
      validatedEntries.add(
          Objects.requireNonNull(entry, "Config entries cannot contain null"));
    }
    this.entries = List.copyOf(validatedEntries);
  }

  public List<ConfigEntry<?>> getEntries() {
    return entries;
  }
}
