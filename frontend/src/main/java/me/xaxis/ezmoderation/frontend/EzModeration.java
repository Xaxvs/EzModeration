package me.xaxis.ezmoderation.frontend;

import java.util.UUID;
import me.xaxis.ezmoderation.backend.player.data.PlayerProfileManager;
import me.xaxis.ezmoderation.backend.settings.ConfigSettings;
import me.xaxis.ezmoderation.frontend.player.BukkitPlayerProfileStore;
import me.xaxis.ezmoderation.frontend.settings.ConfigSettingsLoader;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EzModeration extends JavaPlugin {

  private PlayerProfileManager playerProfileManager;
  private ConfigSettings configSettings;

  public ConfigSettings getConfigSettings() {
    return configSettings;
  }

  public PlayerProfileManager getPlayerProfileManager() {
    return playerProfileManager;
  }

  @Override
  public void onEnable() {
    if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
      getLogger().severe("Failed to create the necessary directories!");
      getPluginLoader().disablePlugin(this);
      return;
    }

    saveDefaultConfig();

    ConfigSettingsLoader configSettingsLoader =
        new ConfigSettingsLoader(getDataFolder().toPath(), getLogger());

    configSettings = configSettingsLoader.load();

    if (configSettings == null) {
      getLogger().severe("Failed to load config settings!");
      getPluginLoader().disablePlugin(this);
      return;
    }

    BukkitPlayerProfileStore playerProfileStore =
        new BukkitPlayerProfileStore(getDataFolder().toPath(), getLogger());

    playerProfileManager = new PlayerProfileManager(playerProfileStore);

    for (Player player : getServer().getOnlinePlayers()) {
      UUID playerId = player.getUniqueId();
      String playerName = player.getName();
      getLogger().info("Loading player profile for: " + playerName);

      playerProfileManager.loadOrCreateProfile(playerId, playerName);
    }
  }

  @Override
  public void onDisable() {
    if (playerProfileManager != null) {
      playerProfileManager.saveAllDirtyProfiles();
    }
  }
}
