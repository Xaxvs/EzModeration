package me.xaxis.ezmoderation.backend.config;

import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

public interface ConfigStore<T> {

  LoadResult<T> get(UUID playerId);

  void save(T profile);

  record LoadResult<T>(Optional<T> profile, boolean requiresSave) {
    public LoadResult {
      Objects.requireNonNull(profile, "profile cannot be null");
    }
  }
}
