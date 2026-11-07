package me.xaxis.ezmoderation.frontend.player;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.ConfigStore;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;

public final class PlayerProfileStore implements ConfigStore<PlayerProfile> {

  private final Path dataFolder;
  private final Logger logger;
  private final Map<UUID, PlayerProfileLoader> profiles = new HashMap<>();

  public PlayerProfileStore(Path dataFolder, Logger logger) {
    if (dataFolder == null) {
      throw new NullPointerException("dataFolder cannot be null");
    }
    if (logger == null) {
      throw new NullPointerException("logger cannot be null");
    }
    this.dataFolder = dataFolder;
    this.logger = logger;
  }

  public Path getPath() {
    return dataFolder.resolve("player-profiles");
  }

  @Override
  public LoadResult<PlayerProfile> get(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    PlayerProfileLoader loader =
        profiles.computeIfAbsent(playerId, id -> new PlayerProfileLoader(getPath(), logger, id));
    PlayerProfile profile = loader.load();
    return new LoadResult<>(Optional.ofNullable(profile), loader.requiresSave());
  }

  @Override
  public void save(PlayerProfile profile) {
    if (profile == null) {
      throw new IllegalArgumentException("profile cannot be null");
    }
    PlayerProfileLoader loader =
        profiles.computeIfAbsent(
            profile.playerId(), id -> new PlayerProfileLoader(getPath(), logger, id));
    loader.save(profile);
  }
}
