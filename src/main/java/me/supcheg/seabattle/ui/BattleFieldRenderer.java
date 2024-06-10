package me.supcheg.seabattle.ui;

import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.BattleFieldService;
import me.supcheg.seabattle.BattleShip;
import me.supcheg.seabattle.OpponentField;
import me.supcheg.seabattle.Position;
import me.supcheg.seabattle.SelfField;
import me.supcheg.seabattle.ShipState;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

@RequiredArgsConstructor
public final class BattleFieldRenderer {
    private final BattleFieldService battleFieldService;

    public void renderField(@NotNull OpponentField field, @NotNull PrintStream out) {
        out.print("    ");
        for (int i = 0; i < 16; i++) {
            out.append(' ').append((char) ('A' + i)).append(' ');
        }
        out.println();
        for (int y = 0; y < field.getSize(); y++) {
            for (int x = -1; x < field.getSize(); x++) {
                if (x == -1) {
                    out.append(' ').append(normalizeLength(String.valueOf(y + 1))).append(' ');
                } else {
                    out.append(' ').append(field.getStateAt(new Position(x, y)).getRendered()).append(' ');
                }
            }
            out.println();
        }
    }

    public void renderField(@NotNull SelfField field, @NotNull PrintStream out) {
        out.print("    ");
        for (int i = 0; i < 16; i++) {
            out.append(' ').append((char) ('A' + i)).append(' ');
        }
        out.println();
        for (int y = 0; y < field.getSize(); y++) {
            for (int x = -1; x < field.getSize(); x++) {
                if (x == -1) {
                    out.append(' ').append(normalizeLength(String.valueOf(y + 1))).append(' ');
                } else {
                    out.append(' ').append(getShipStateAt(field, new Position(x, y)).getRendered()).append(' ');
                }
            }
            out.println();
        }
    }

    @NotNull
    private String normalizeLength(@NotNull String s) {
        return s.length() == 2 ? s : ' ' + s;
    }

    @NotNull
    private ShipState getShipStateAt(@NotNull SelfField field, @NotNull Position position) {
        BattleShip ship = battleFieldService.findShipByAnyPosition(field, position);
        if (ship == null) {
            if (battleFieldService.findShipByNearPosition(field, position) != null) {
                return ShipState.CLOSE;
            }

            return ShipState.EMPTY;
        }

        if (ship.isDead()) {
            return ShipState.DEATH;
        }

        if (!ship.getAlivePositions().contains(position)) {
            return ShipState.HIT;
        }

        return ShipState.ALIVE;
    }

}
