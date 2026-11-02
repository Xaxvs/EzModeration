package me.xaxis.ezmoderation.backend.player;

import me.xaxis.ezmoderation.backend.config.ConfigFile;

import java.nio.file.Path;
import java.util.UUID;

public class PlayerProfileManager {
  private final Path dataFolder;

  public PlayerProfileManager(Path dataFolder) {
    this.dataFolder = dataFolder;
  }


  public PlayerProfile createPlayerProfile(UUID playerId, String playerName) {
    ConfigFile configFile = new ConfigFile(dataFolder.resolve("player-profiles"), playerId.toString() + ".yml");
    configFile.initialize();
    return new PlayerProfile(playerId, playerName);

  }

}
