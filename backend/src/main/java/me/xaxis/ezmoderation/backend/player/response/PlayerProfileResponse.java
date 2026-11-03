package me.xaxis.ezmoderation.backend.player.response;

public record PlayerProfileResponse(
    String uuid,
    String name,
    PlayerActivityResponse playerActivity,
    PlayerInfractionHistoryResponse playerInfractionHistory) {}
