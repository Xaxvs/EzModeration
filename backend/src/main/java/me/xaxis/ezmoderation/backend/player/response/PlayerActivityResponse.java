package me.xaxis.ezmoderation.backend.player.response;

import java.time.Instant;

public record PlayerActivityResponse(
    boolean online,
    Instant lastLogin,
    Instant lastLogout,
    String lastKnownName
) {}
