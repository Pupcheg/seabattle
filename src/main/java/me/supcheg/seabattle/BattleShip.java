package me.supcheg.seabattle;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
public final class BattleShip {
    public BattleShip(@NotNull List<Position> allPositions) {
        this.allPositions = allPositions;
        this.alivePositions = new ArrayList<>(allPositions);
    }

    private final List<Position> allPositions;
    private final List<Position> alivePositions;
}
