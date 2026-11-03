package me.xaxis.ezmoderation.backend.player.data;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.config.ConfigStore;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.response.PlayerActivityResponse;
import me.xaxis.ezmoderation.backend.player.response.PlayerInfractionHistoryResponse;
import me.xaxis.ezmoderation.backend.player.response.PlayerProfileResponse;

public class PlayerProfileManager {

  private final Map<UUID, ConfigStore.Result<PlayerProfile>> playerProfiles = new HashMap<>();
  private final ConfigStore<PlayerProfile> store;

  public PlayerProfileManager(ConfigStore<PlayerProfile> store) {
    if (store == null) {
      throw new NullPointerException("store cannot be null");
    }
    this.store = store;
  }

  public Iterable<AbstractMap.SimpleEntry<UUID, PlayerProfileResponse>> getPlayerProfiles() {
    return playerProfiles.entrySet().stream()
        .map(entry ->
            new AbstractMap.SimpleEntry<>(
                entry.getKey(), toProfileResponse(entry.getValue().profile())))
        .toList();
  }

  private PlayerActivityResponse toActivityResponse(PlayerProfile playerProfile) {
    return new PlayerActivityResponse(
        playerProfile.getActivity().isOnline(),
        playerProfile.getActivity().getLastLogin(),
        playerProfile.getActivity().getLastLogout(),
        playerProfile.getActivity().getLastKnownName());
  }

  private PlayerInfractionHistoryResponse toInfractionHistoryResponse(PlayerProfile playerProfile) {
    return new PlayerInfractionHistoryResponse(
        playerProfile.getInfractionHistory().getInfractions());
  }

  private PlayerProfileResponse toProfileResponse(PlayerProfile playerProfile) {
    return new PlayerProfileResponse(
        playerProfile.getPlayerId().toString(),
        playerProfile.getActivity().getLastKnownName(),
        toActivityResponse(playerProfile),
        toInfractionHistoryResponse(playerProfile));
  }

  public void saveAllDirtyProfiles() {
    for (var entry : playerProfiles.entrySet()) {
      if (entry.getValue().isDirty()) {
        savePlayerProfile(entry.getKey());
      }
    }
  }

  public PlayerProfileEditor editProfile(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    ConfigStore.Result<PlayerProfile> wrapper = playerProfiles.get(playerId);
    if (wrapper == null) {
      throw new IllegalStateException("Player profile not found for playerId: " + playerId);
    }
    return new PlayerProfileEditor(wrapper.profile(), wrapper::setDirty);
  }

  public Optional<PlayerProfileResponse> getProfile(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    if (playerProfiles.containsKey(playerId)) {
      return Optional.of(toProfileResponse(playerProfiles.get(playerId).profile()));
    } else return Optional.empty();
  }

  public PlayerProfileResponse loadOrCreateProfile(UUID playerId, String playerName) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    if (playerName == null || playerName.isBlank()) {
      throw new IllegalArgumentException("playerName cannot be null or blank");
    }
    return toProfileResponse(
        playerProfiles
            .computeIfAbsent(playerId, id -> initializePlayerProfile(playerId, playerName))
            .profile());
  }

  private ConfigStore.Result<PlayerProfile> initializePlayerProfile(
      UUID playerId, String playerName) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    if (playerName == null || playerName.isBlank()) {
      throw new IllegalArgumentException("playerName cannot be null or blank");
    }
    ConfigStore.Result<PlayerProfile> result = store.load(playerId);
    PlayerProfile playerProfile = result.profile();
    if (playerProfile != null && !playerId.equals(playerProfile.getPlayerId())) {
      throw new IllegalStateException("Profile store returned a different player ID");
    }
    if (playerProfile == null) {
      playerProfile = new PlayerProfile(playerId, playerName);
      result.setDirty();
    }
    return new ConfigStore.Result<>(result.isDirty(), playerProfile);
  }

  private void savePlayerProfile(ConfigStore.Result<PlayerProfile> wrapper) {
    if (wrapper == null) {
      throw new IllegalArgumentException("wrapper cannot be null");
    }
    if (!wrapper.isDirty()) {
      return;
    }
    store.save(wrapper);
    wrapper.setClean();
  }

  public void savePlayerProfile(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }

    ConfigStore.Result<PlayerProfile> wrapper = playerProfiles.get(playerId);
    if (wrapper == null || !wrapper.isDirty()) {
      return;
    }
    store.save(wrapper);
    wrapper.setClean();
  }
}
