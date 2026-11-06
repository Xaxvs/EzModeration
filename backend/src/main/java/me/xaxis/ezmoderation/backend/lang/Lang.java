package me.xaxis.ezmoderation.backend.lang;

public record Lang(
    String noPermissions,
    String senderCantExecuteCommand,
    String playerNotFound,
    String historyCommandUsage) {

  @Override
  public String noPermissions() {
    return noPermissions;
  }

  public String playerNotFound(String playerName) {
    return playerNotFound.replace("{player}", playerName);
  }
}
