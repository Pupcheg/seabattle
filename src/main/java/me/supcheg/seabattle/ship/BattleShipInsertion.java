package me.supcheg.seabattle.ship;

import lombok.Data;

@Data
public final class BattleShipInsertion {
    private final Position position;
    private final int length;
    private final Rotation rotation;
}
