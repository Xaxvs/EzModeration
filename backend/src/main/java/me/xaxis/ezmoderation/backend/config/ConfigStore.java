package me.xaxis.ezmoderation.backend.config;

import java.util.Optional;
import java.util.UUID;

public interface ConfigStore<T> {

  Optional<T> load(UUID playerId);

  void save(T profile);
}
