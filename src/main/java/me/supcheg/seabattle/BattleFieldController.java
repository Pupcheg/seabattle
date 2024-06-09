package me.supcheg.seabattle;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BattleFieldController {

    public void acceptPlayerMove(@NotNull BattleField battleField, @NotNull Position position) {
        BattleShip ship = findShipByAlivePosition(battleField, position);
        if (ship != null) {
            ship.getAlivePositions().remove(position);
        }
    }

    @Nullable
    public BattleShip findShipByAnyPosition(@NotNull BattleField field, @NotNull Position position) {
        return field.getLeftShips().stream()
                .filter(ship -> ship.getAllPositions().contains(position))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public BattleShip findShipByAlivePosition(@NotNull BattleField field, @NotNull Position position) {
        return field.getLeftShips().stream()
                .filter(ship -> ship.getAlivePositions().contains(position))
                .findFirst()
                .orElse(null);
    }
}
