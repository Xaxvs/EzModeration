package me.xaxis.ezmoderation.frontend.utils;

import org.bukkit.ChatColor;

public class ChatUtils {

  private ChatUtils() {}

  public static String chat(String message) {
    return ChatColor.translateAlternateColorCodes('&', message);
  }
}
