package me.xaxis.ezmoderation.frontend.player;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import me.xaxis.ezmoderation.backend.config.ConfigStore;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.data.PlayerProfileSchema;

public final class BukkitPlayerProfileStore implements ConfigStore<PlayerProfile> {

  private final Path dataFolder;
  private final Logger logger;
  private final Map<UUID, PlayerProfileLoader> profiles = new HashMap<>();

  public BukkitPlayerProfileStore(Path dataFolder, Logger logger) {
    if (dataFolder == null) {
      throw new NullPointerException("dataFolder cannot be null");
    }
    if (logger == null) {
      throw new NullPointerException("logger cannot be null");
    }
    this.dataFolder = dataFolder;
    this.logger = logger;
  }

  @Override
  public Optional<PlayerProfile> load(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    if (profiles.containsKey(playerId)) {
      PlayerProfileLoader loader = profiles.get(playerId);
      PlayerProfile profile = loader.load();
      return Optional.ofNullable(profile);
    }

    PlayerProfileLoader loader = new PlayerProfileLoader(dataFolder, logger, playerId);
    profiles.put(playerId, loader);
    PlayerProfile profile = loader.load();
    return Optional.ofNullable(profile);
  }

  @Override
  public void save(PlayerProfile profile) {
    if (profile == null) {
      throw new IllegalArgumentException("profile cannot be null");
    }
    if (profiles.containsKey(profile.getPlayerId())) {
      PlayerProfileLoader loader = profiles.get(profile.getPlayerId());
      loader.write(PlayerProfileSchema.serialize(profile));
      loader.save();
      return;
    }

    UUID playerId = profile.getPlayerId();
    PlayerProfileLoader loader = new PlayerProfileLoader(dataFolder, logger, playerId);
    profiles.put(playerId, loader);
    loader.write(PlayerProfileSchema.serialize(profile));
    loader.save();
  }
}
