package me.xaxis.ezmoderation.frontend.commands;

import java.util.UUID;
import me.xaxis.ezmoderation.backend.lang.Lang;
import me.xaxis.ezmoderation.backend.player.PlayerProfileManager;
import me.xaxis.ezmoderation.frontend.inventory.PlayerInfractionsGUI;
import me.xaxis.ezmoderation.frontend.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PlayerHistoryCommand implements CommandExecutor {

  private final Lang lang;
  private final PlayerProfileManager profileManager;

  public PlayerHistoryCommand(PlayerProfileManager profileManager, Lang lang) {
    this.profileManager = profileManager;
    this.lang = lang;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    if (!sender.hasPermission("ezmoderation.history")) {
      sender.sendMessage(ChatUtils.chat(lang.noPermissions()));
      return true;
    }

    if (sender instanceof ConsoleCommandSender console) {
      handleConsoleCommand(console, args);
    } else if (!(sender instanceof Player)) {
      sender.sendMessage(ChatUtils.chat(lang.senderCantExecuteCommand()));
      return true;
    }

    if (args.length < 1) {
      sender.sendMessage(ChatUtils.chat(lang.historyCommandUsage()));
      return true;
    }

    Player player = (Player) sender;

    String playerName = args[0];
    UUID playerId = profileManager.getPlayerIdByName(playerName);
    if (playerId == null) {
      player.sendMessage(ChatUtils.chat(lang.playerNotFound(playerName)));
      return true;
    }
    var profile = profileManager.loadOrCreateProfile(playerId, playerName);

    if (profile == null) {
      player.sendMessage(ChatUtils.chat(lang.playerNotFound(playerName)));
      return true;
    }
    // todo gui for player
    PlayerInfractionsGUI gui =
        new PlayerInfractionsGUI(profile.playerInfractionHistory().infractions());

    player.openInventory(gui.getInventory());

    return true;
  }

  private void handleConsoleCommand(ConsoleCommandSender console, String[] args) {
    if (args.length < 1) {
      console.sendMessage(ChatUtils.chat(lang.historyCommandUsage()));
      return;
    }

    String playerName = args[0];
    UUID playerId = profileManager.getPlayerIdByName(playerName);
    if (playerId == null) {
      console.sendMessage(ChatUtils.chat(lang.playerNotFound(playerName)));
      return;
    }
    var profile = profileManager.loadOrCreateProfile(playerId, playerName);

    if (profile == null) {
      console.sendMessage(ChatUtils.chat(lang.playerNotFound(playerName)));
      return;
    }

    console.sendMessage(ChatUtils.chat("&6History for player: &e" + playerName));
    for (var infraction : profile.playerInfractionHistory().infractions()) {
      console.sendMessage(
          ChatUtils.chat(
              "&7- &e"
                  + infraction.getType()
                  + " &7on &e"
                  + infraction.getTimestamp()
                  + " &7by &e"
                  + infraction.getIssuerName()
                  + " &7Reason: &e"
                  + infraction.getReason()
                  + (infraction.expiresAt().isPresent()
                      ? " &7Expires: &e" + infraction.expiresAt().get()
                      : " &7Expires: &eNever")));
    }
  }
}
