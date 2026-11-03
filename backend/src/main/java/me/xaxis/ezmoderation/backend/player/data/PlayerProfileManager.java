package me.xaxis.ezmoderation.backend.player.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.config.ConfigStore;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;

public class PlayerProfileManager {

  private final Map<UUID, ConfigStore.Result<PlayerProfile>> playerProfiles = new HashMap<>();
  private final ConfigStore<PlayerProfile> store;

  public PlayerProfileManager(ConfigStore<PlayerProfile> store) {
    if (store == null) {
      throw new NullPointerException("store cannot be null");
    }
    this.store = store;
  }

  public Iterable<Map.Entry<UUID, ConfigStore.Result<PlayerProfile>>> getPlayerProfiles() {
    return playerProfiles.entrySet();
  }

  public void markDirty(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    ConfigStore.Result<PlayerProfile> wrapper = playerProfiles.get(playerId);
    if (wrapper != null) {
      wrapper.setDirty(true);
    }
  }

  public void saveAllDirtyProfiles() {
    for (Map.Entry<UUID, ConfigStore.Result<PlayerProfile>> entry : getPlayerProfiles()) {
      ConfigStore.Result<PlayerProfile> wrapper = entry.getValue();
      if (wrapper.isDirty()) {
        store.save(wrapper);
        wrapper.setDirty(false);
      }
    }
  }

  public PlayerProfile getPlayerProfile(UUID playerId, String playerName) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    if (playerName == null) {
      throw new IllegalArgumentException("playerName cannot be null");
    }
    return playerProfiles
        .computeIfAbsent(playerId, id -> initializePlayerProfile(id, playerName))
        .profile();
  }

  private ConfigStore.Result<PlayerProfile> initializePlayerProfile(
      UUID playerId, String playerName) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    ConfigStore.Result<PlayerProfile> result = store.load(playerId);
    PlayerProfile playerProfile = result.profile();
    if (playerProfile != null && !playerId.equals(playerProfile.getPlayerId())) {
      throw new IllegalStateException("Profile store returned a different player ID");
    }
    if (playerProfile == null) {
      playerProfile = new PlayerProfile(playerId, playerName);
    }
    return new ConfigStore.Result<>(result.isDirty(), playerProfile);
  }

  public void savePlayerProfile(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    ConfigStore.Result<PlayerProfile> wrapper = playerProfiles.get(playerId);
    if (wrapper != null && wrapper.isDirty()) {
      store.save(wrapper);
      wrapper.setDirty(false);
    }
  }
}
