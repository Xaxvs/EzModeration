package me.xaxis.ezmoderation.frontend;

import me.xaxis.ezmoderation.backend.player.data.PlayerProfileManager;
import me.xaxis.ezmoderation.frontend.player.BukkitPlayerProfileStore;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EzModeration extends JavaPlugin {

  @Override
  public void onEnable() {
    if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
      getLogger().severe("Failed to create the necessary directories!");
      getPluginLoader().disablePlugin(this);
      return;
    }

    BukkitPlayerProfileStore playerProfileStore =
        new BukkitPlayerProfileStore(getDataFolder().toPath(), getLogger());
    PlayerProfileManager playerProfileManager = new PlayerProfileManager(playerProfileStore);

    for (Player player : getServer().getOnlinePlayers()) {
      getLogger().info("Loading player profile for: " + player.getName());
      playerProfileManager.getPlayerProfile(player.getUniqueId(), player.getName());
    }

    saveDefaultConfig();
  }
}
