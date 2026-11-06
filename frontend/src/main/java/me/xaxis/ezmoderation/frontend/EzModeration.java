package me.xaxis.ezmoderation.frontend;

import java.time.Instant;
import java.util.UUID;
import me.xaxis.ezmoderation.backend.player.data.PlayerProfileManager;
import me.xaxis.ezmoderation.backend.settings.ConfigSettings;
import me.xaxis.ezmoderation.frontend.player.PlayerLifecycleListener;
import me.xaxis.ezmoderation.frontend.player.PlayerProfileStore;
import me.xaxis.ezmoderation.frontend.settings.ConfigSettingsLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class EzModeration extends JavaPlugin {

  private PlayerProfileManager playerProfileManager;
  private ConfigSettings configSettings;
  private ConfigSettingsLoader configSettingsLoader;
  private BukkitTask autoSaveTask;

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

    configSettingsLoader = new ConfigSettingsLoader(getDataFolder().toPath(), getLogger());

    configSettings = configSettingsLoader.load();

    if (configSettings == null) {
      getLogger().severe("Failed to load config settings!");
      getPluginLoader().disablePlugin(this);
      return;
    }

    PlayerProfileStore playerProfileStore =
        new PlayerProfileStore(getDataFolder().toPath(), getLogger());

    playerProfileManager = new PlayerProfileManager(playerProfileStore);
    getServer()
        .getPluginManager()
        .registerEvents(new PlayerLifecycleListener(playerProfileManager), this);

    for (Player player : getServer().getOnlinePlayers()) {
      UUID playerId = player.getUniqueId();
      String playerName = player.getName();
      getLogger().info("Loading player profile for: " + playerName);

      playerProfileManager.loadOrCreateProfile(playerId, playerName);
      playerProfileManager.editProfile(playerId).login(Instant.now(), playerName);
    }

    autoSaveTask =
        Bukkit.getServer()
            .getScheduler()
            .runTaskTimer(
                this,
                () -> {
                  getLogger().info("Auto-saving all player profiles...");
                  playerProfileManager.saveAllDirtyProfiles();
                },
                configSettings.profileAutoSaveDelay(),
                configSettings.profileAutoSaveDelay());
  }

  @Override
  public void onDisable() {
    if (playerProfileManager != null) {
      playerProfileManager.saveAllDirtyProfiles();
    }
    if (configSettingsLoader != null) {
      configSettingsLoader.save();
    }
    if (autoSaveTask != null && !autoSaveTask.isCancelled()) {
      autoSaveTask.cancel();
    }
  }
}
