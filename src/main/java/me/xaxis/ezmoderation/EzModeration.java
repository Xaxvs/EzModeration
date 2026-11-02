package me.xaxis.ezmoderation;

import org.bukkit.plugin.java.JavaPlugin;

public class EzModeration extends JavaPlugin {

  @Override
  public void onEnable() {

    if (!getDataFolder().exists()) {
      if (!getDataFolder().mkdirs()) {
        getLogger().severe("Failed to create the necessary directories!");
        getPluginLoader().disablePlugin(this);
        return;
      }
    }

    saveDefaultConfig();
  }

  @Override
  public void onDisable() {}
}
