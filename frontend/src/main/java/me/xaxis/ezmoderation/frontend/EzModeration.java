package me.xaxis.ezmoderation.frontend;

import org.bukkit.plugin.java.JavaPlugin;

public class EzModeration extends JavaPlugin {

  @Override
  public void onEnable() {
    if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
      getLogger().severe("Failed to create the necessary directories!");
      getPluginLoader().disablePlugin(this);
      return;
    }

    saveDefaultConfig();
  }
}
