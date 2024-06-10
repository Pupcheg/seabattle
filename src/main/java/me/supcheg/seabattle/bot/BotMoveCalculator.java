package me.supcheg.seabattle.bot;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.field.BattleFieldService;
import me.supcheg.seabattle.field.OpponentField;
import me.supcheg.seabattle.ship.Position;
import me.supcheg.seabattle.ship.ShipState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

@RequiredArgsConstructor
public final class BotMoveCalculator {
    private final OpponentField field;
    private final BattleFieldService service;
    private final RandomGenerator random = ThreadLocalRandom.current();
    private final List<IntPair> deltas = List.of(
            new IntPair(1, 0),
            new IntPair(-1, 0),
            new IntPair(0, 1),
            new IntPair(0, -1)
    );

    @NotNull
    public Position calculateMove() {
        Position position = findHitShip();
        if (position != null) {
            Position nearlyHitPosition = findExistingNearlyHit(position);

            if (nearlyHitPosition != null) {
                Position targetPosition = new Position(
                        -(position.getX() - nearlyHitPosition.getX()),
                        -(position.getY() - nearlyHitPosition.getY())
                );
                if (service.isInField(targetPosition, field.getSize())) {
                    return targetPosition;
                }
            }

            Position newNearlyHit = findNewNearlyHit(position);
            if (newNearlyHit != null) {
                return newNearlyHit;
            }
        }

        return findRandomEmptyPosition();
    }

    @Nullable
    private Position findNewNearlyHit(@NotNull Position start) {
        int x = start.getX();
        int y = start.getY();

        for (IntPair delta : deltas) {
            int currentX = delta.getX() + x;
            int currentY = delta.getY() + y;
            Position currentPosition = new Position(currentX, currentY);

            if (!service.canHit(field, currentPosition)) {
                continue;
            }

            if (service.isInField(currentPosition, field.getSize())) {
                return currentPosition;
            }
        }
        return null;
    }

    @Nullable
    private Position findExistingNearlyHit(@NotNull Position start) {
        int x = start.getX();
        int y = start.getY();

        for (IntPair delta : deltas) {
            int currentX = delta.getX() + x;
            int currentY = delta.getY() + y;
            Position currentPosition = new Position(currentX, currentY);

            if (!service.canHit(field, currentPosition)) {
                continue;
            }

            if (!service.isInField(currentPosition, field.getSize())) {
                continue;
            }
            if (field.getStateAt(currentPosition) == ShipState.HIT) {
                return currentPosition;
            }
        }
        return null;
    }

    @NotNull
    private Position findRandomEmptyPosition() {
        Position position;
        do {
            position = new Position(random.nextInt(field.getSize()), random.nextInt(field.getSize()));
        } while (!service.canHit(field, position));
        return position;
    }

    private Position findHitShip() {
        return service.findAnyPositionWithState(field, ShipState.HIT);
    }

    @Data
    private static final class IntPair {
        private final int x;
        private final int y;
    }
}
