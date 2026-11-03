package me.xaxis.ezmoderation.frontend.player;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.ConfigStore;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.PlayerProfileSchema;

public final class BukkitPlayerProfileStore implements ConfigStore<PlayerProfile> {

  private final Path dataFolder;
  private final Logger logger;
  private final Map<UUID, PlayerProfileLoader> profiles = new HashMap<>();

  public BukkitPlayerProfileStore(Path dataFolder, Logger logger) {
    this.dataFolder = dataFolder;
    this.logger = logger;
  }

  @Override
  public Result<PlayerProfile> load(UUID playerId) {
    if (profiles.containsKey(playerId)) {
      PlayerProfileLoader loader = profiles.get(playerId);
      return new Result<>(false, loader.load());
    }

    PlayerProfileLoader loader = new PlayerProfileLoader(dataFolder, logger, playerId);
    profiles.put(playerId, loader);
    return new Result<>(loader.shouldSave(), loader.load());
  }

  @Override
  public void save(Result<PlayerProfile> profile) {
    if (profile == null) {
      throw new IllegalArgumentException("profile cannot be null");
    }
    if (profiles.containsKey(profile.profile().getPlayerId())) {
      if (!profile.isDirty()) return;
      PlayerProfileLoader loader = profiles.get(profile.profile().getPlayerId());
      loader.write(PlayerProfileSchema.serialize(profile.profile()));
      loader.save();
      return;
    }

    UUID playerId = profile.profile().getPlayerId();
    PlayerProfileLoader loader = new PlayerProfileLoader(dataFolder, logger, playerId);
    loader.write(PlayerProfileSchema.serialize(profile.profile()));
    loader.save();
  }
}
