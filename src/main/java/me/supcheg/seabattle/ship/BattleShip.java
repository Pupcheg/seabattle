package me.supcheg.seabattle.ship;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public final class BattleShip {
    public BattleShip(@NotNull List<Position> allPositions) {
        this.allPositions = List.copyOf(allPositions);
        this.alivePositions = new ArrayList<>(allPositions);
        this.nearPositions = buildNearPositions(allPositions);
    }

    @NotNull
    @Unmodifiable
    private List<Position> buildNearPositions(@NotNull List<Position> positions) {
        Set<Position> nearPositions = new HashSet<>();
        for (Position position : positions) {
            int x = position.getX();
            int y = position.getY();

            for (int deltaX = -1; deltaX <= 1; deltaX++) {
                for (int deltaY = -1; deltaY <= 1; deltaY++) {
                    nearPositions.add(new Position(x + deltaX, y + deltaY));
                }
            }
        }
        positions.forEach(nearPositions::remove);
        return List.copyOf(nearPositions);
    }

    private final List<Position> allPositions;
    private final List<Position> alivePositions;
    private final List<Position> nearPositions;

    public boolean isAlive() {
        return !alivePositions.isEmpty();
    }

    public boolean isDead() {
        return alivePositions.isEmpty();
    }
}
