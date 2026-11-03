package me.xaxis.ezmoderation.backend.config;

import java.util.UUID;

public interface ConfigStore<T> {

  Result<T> load(UUID playerId);

  void save(Result<T> profile);

  class Result<T> {
    private final T value;
    private boolean isDirty;

    public Result(boolean isDirty, T value) {
      this.isDirty = isDirty;
      this.value = value;
    }

    public boolean isDirty() {
      return isDirty;
    }

    public void setDirty(boolean dirty) {
      isDirty = dirty;
    }

    public T profile() {
      return value;
    }
  }
}
