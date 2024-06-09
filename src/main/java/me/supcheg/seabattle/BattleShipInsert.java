package me.supcheg.seabattle;

import lombok.Data;

@Data
public final class BattleShipInsert {
    private final Position position;
    private final int length;
    private final Rotation rotation;

    public enum Rotation {
        VERTICAL, HORIZONTAL
    }
}
