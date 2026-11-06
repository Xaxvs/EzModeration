package me.xaxis.ezmoderation.backend.player.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.config.ConfigStore;
import me.xaxis.ezmoderation.backend.player.PlayerProfile;
import me.xaxis.ezmoderation.backend.player.response.PlayerActivityResponse;
import me.xaxis.ezmoderation.backend.player.response.PlayerInfractionHistoryResponse;
import me.xaxis.ezmoderation.backend.player.response.PlayerProfileResponse;

public class PlayerProfileManager {

  private final Map<UUID, PlayerProfile> playerProfiles = new HashMap<>();
  private final Set<UUID> dirtyProfiles = new HashSet<>();
  private final Map<String, UUID> nameToIdMap = new HashMap<>();
  private final ConfigStore<PlayerProfile> store;

  public PlayerProfileManager(ConfigStore<PlayerProfile> store) {
    if (store == null) {
      throw new NullPointerException("store cannot be null");
    }
    this.store = store;
  }

  public List<PlayerProfileResponse> getPlayerProfiles() {
    return playerProfiles.values().stream().map(this::toProfileResponse).toList();
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
      if (dirtyProfiles.contains(entry.getKey())) {
        savePlayerProfile(entry.getValue());
      }
    }
  }

  public PlayerProfileEditor editProfile(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    PlayerProfile wrapper = playerProfiles.get(playerId);
    if (wrapper == null) {
      throw new IllegalStateException("Player profile not found for playerId: " + playerId);
    }
    return new PlayerProfileEditor(wrapper, () -> dirtyProfiles.add(playerId));
  }

  public Optional<PlayerProfileResponse> findProfile(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    if (playerProfiles.containsKey(playerId)) {
      PlayerProfile profile = playerProfiles.get(playerId);
      nameToIdMap.put(profile.getActivity().getLastKnownName(), playerId);
      return Optional.of(toProfileResponse(profile));
    }
    return Optional.empty();
  }

  public UUID getPlayerIdByName(String playerName) {
    if (playerName == null || playerName.isBlank()) {
      throw new IllegalArgumentException("playerName cannot be null or blank");
    }
    return nameToIdMap.get(playerName);
  }

  public PlayerProfileResponse loadOrCreateProfile(UUID playerId, String playerName) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    if (playerName == null || playerName.isBlank()) {
      throw new IllegalArgumentException("playerName cannot be null or blank");
    }
    nameToIdMap.put(playerName, playerId);
    return toProfileResponse(
        playerProfiles.computeIfAbsent(playerId, id -> initializePlayerProfile(id, playerName)));
  }

  private PlayerProfile initializePlayerProfile(UUID playerId, String playerName) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }
    if (playerName == null || playerName.isBlank()) {
      throw new IllegalArgumentException("playerName cannot be null or blank");
    }
    ConfigStore.LoadResult<PlayerProfile> loadResult = store.get(playerId);
    Optional<PlayerProfile> profileOptional = loadResult.profile();
    PlayerProfile playerProfile;
    if (profileOptional.isPresent() && !playerId.equals(profileOptional.get().getPlayerId())) {
      throw new IllegalStateException("Profile store returned a different player ID");
    }
    if (profileOptional.isEmpty()) {
      playerProfile = new PlayerProfile(playerId, playerName);
      dirtyProfiles.add(playerId);
    } else {
      playerProfile = profileOptional.get();
      if (loadResult.requiresSave()) {
        dirtyProfiles.add(playerId);
      }
    }

    return playerProfile;
  }

  private void savePlayerProfile(PlayerProfile profile) {
    if (profile == null) {
      throw new IllegalArgumentException("profile cannot be null");
    }
    if (!dirtyProfiles.contains(profile.getPlayerId())) {
      return;
    }
    store.save(profile);
    dirtyProfiles.remove(profile.getPlayerId());
  }

  public void savePlayerProfile(UUID playerId) {
    if (playerId == null) {
      throw new IllegalArgumentException("playerId cannot be null");
    }

    PlayerProfile profile = playerProfiles.get(playerId);
    if (profile == null || !dirtyProfiles.contains(playerId)) {
      return;
    }
    store.save(profile);
    dirtyProfiles.remove(playerId);
  }
}
