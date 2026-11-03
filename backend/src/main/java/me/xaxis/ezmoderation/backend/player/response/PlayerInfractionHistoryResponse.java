package me.xaxis.ezmoderation.backend.player.response;

import java.util.List;
import me.xaxis.ezmoderation.backend.player.infraction.Infraction;

public record PlayerInfractionHistoryResponse(
    List<Infraction> infractions) {

  public PlayerInfractionHistoryResponse {
    infractions = List.copyOf(infractions);
  }

  @Override
  public List<Infraction> infractions() {
    return infractions.stream().toList();
  }
}
