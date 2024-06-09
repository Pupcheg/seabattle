package me.supcheg.seabattle;

import lombok.Data;

@Data
public final class BattleShipInsertion {
    private final Position position;
    private final int length;
    private final Rotation rotation;
}
