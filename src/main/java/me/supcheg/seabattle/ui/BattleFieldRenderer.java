package me.supcheg.seabattle.ui;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.BattleField;
import me.supcheg.seabattle.BattleFieldController;
import me.supcheg.seabattle.BattleShip;
import me.supcheg.seabattle.Position;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

@RequiredArgsConstructor
public final class BattleFieldRenderer {
    private final BattleFieldController battleFieldController;

    public void renderSelfField(@NotNull BattleField field, @NotNull Writer out) throws IOException {
        out.append("    ");
        for (int i = 0; i < 16; i++) {
            out.append(' ').append((char) ('A' + i)).append(' ');
        }
        out.append('\n');
        for (int y = 0; y < 16; y++) {
            for (int x = -1; x < 16; x++) {
                if (x == -1) {
                    out.append(' ').append(normalizeLength(String.valueOf(y + 1))).append(' ');
                } else {
                    out.append('[').append(getShipStateAt(field, new Position(x, y)).rendered).append(']');
                }
            }
            out.append('\n');
        }
    }

    @NotNull
    private String normalizeLength(@NotNull String s) {
        return s.length() == 2 ? s : ' ' + s;
    }

    @NotNull
    private ShipState getShipStateAt(@NotNull BattleField field, @NotNull Position position) {
        BattleShip ship = battleFieldController.findShipByAnyPosition(field, position);
        if (ship == null) {
            return ShipState.EMPTY;
        }

        if (ship.getAlivePositions().isEmpty()) {
            return ShipState.DEATH;
        }

        if (!ship.getAlivePositions().contains(position)) {
            return ShipState.HIT;
        }

        return ShipState.ALIVE;
    }

    @RequiredArgsConstructor
    private enum ShipState {
        ALIVE(' '), HIT('-'), DEATH('X'), EMPTY('~');

        private final char rendered;
    }
}
