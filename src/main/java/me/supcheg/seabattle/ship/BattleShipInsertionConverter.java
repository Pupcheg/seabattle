package me.supcheg.seabattle.ship;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BattleShipInsertionConverter {
    @NotNull
    public BattleShip insertionToShip(@NotNull BattleShipInsertion insertion) {
        int length = insertion.getLength();

        List<Position> positions = new ArrayList<>(length);

        Position startPosition = insertion.getPosition();
        for (int i = 0; i < length; i++) {
            Position position = switch (insertion.getRotation()) {
                case VERTICAL -> new Position(startPosition.getX(), startPosition.getY() + i);
                case HORIZONTAL -> new Position(startPosition.getX() + i, startPosition.getY());
            };
            positions.add(position);
        }

        return new BattleShip(positions);
    }

    @NotNull
    public BattleShipInsertion shipToInsertion(@NotNull BattleShip ship) {
        List<Position> sortedPositions = new ArrayList<>(ship.getAllPositions());
        sortedPositions.sort(Comparator.comparingInt(Position::getX).thenComparing(Position::getY));

        int length = ship.getAllPositions().size();
        Position position = sortedPositions.getFirst();
        Rotation rotation;
        if (sortedPositions.size() == 1) {
            rotation = Rotation.VERTICAL;
        } else {
            Position lastPosition = sortedPositions.getLast();
            rotation = lastPosition.getX() != position.getX() ? Rotation.HORIZONTAL : Rotation.VERTICAL;
        }

        return new BattleShipInsertion(position, length, rotation);
    }
}
