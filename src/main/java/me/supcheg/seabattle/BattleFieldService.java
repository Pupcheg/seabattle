package me.supcheg.seabattle;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

@Data
public final class BattleFieldService {
    private final BattleShipInsertionConverter converter;

    @NotNull
    public ShipState acceptPlayerMove(@NotNull SelfField battleField, @NotNull Position position) {
        BattleShip ship = findShipByAlivePosition(battleField, position);
        if (ship != null) {
            ship.getAlivePositions().remove(position);
            return ship.isAlive() ? ShipState.HIT : ShipState.DEATH;
        }
        return ShipState.CLOSE;
    }

    public void acceptPlayerMove(@NotNull OpponentField field, @NotNull Position position, @NotNull ShipState state) {
        if (state == ShipState.DEATH) {
            walkDeadShip(field, position);
            return;
        }

        field.setStateAt(position, state);
    }

    private void walkDeadShip(@NotNull OpponentField field, @NotNull Position position) {
        int x = position.getX();
        int y = position.getY();

        field.setStateAt(position, ShipState.DEATH);
        for (int deltaX = -1; deltaX <= 1; deltaX++) {
            int currentX = x + deltaX;
            if (currentX < 0 || currentX >= field.getSize()) {
                continue;
            }

            for (int deltaY = -1; deltaY <= 1; deltaY++) {

                int currentY = y + deltaY;
                if (currentY < 0 || currentY >= field.getSize()) {
                    continue;
                }

                Position currentPosition = new Position(currentX, currentY);
                ShipState currentState = field.getStateAt(currentPosition);
                if (currentState == ShipState.HIT) {
                    walkDeadShip(field, currentPosition);
                } else if (currentState == ShipState.EMPTY) {
                    field.setStateAt(currentPosition, ShipState.CLOSE);
                }
            }
        }
    }

    @Nullable
    public BattleShip findShipByAnyPosition(@NotNull SelfField field, @NotNull Position position) {
        return findShipByPosition(field, position, BattleShip::getAllPositions);
    }

    @Nullable
    public BattleShip findShipByAlivePosition(@NotNull SelfField field, @NotNull Position position) {
        return findShipByPosition(field, position, BattleShip::getAlivePositions);
    }

    @Nullable
    public BattleShip findShipByNearPosition(@NotNull SelfField field, @NotNull Position position) {
        return findShipByPosition(field, position, BattleShip::getNearPositions);
    }

    @Nullable
    private BattleShip findShipByPosition(@NotNull SelfField field, @NotNull Position position,
                                          @NotNull Function<BattleShip, Collection<Position>> function) {
        return field.getLeftShips().stream()
                .filter(ship -> function.apply(ship).contains(position))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public PlaceResult canPlace(@NotNull SelfField field, @NotNull BattleShipInsertion insertion) {
        int size = field.getSize();
        BattleShip ship = insertionToShip(insertion);

        for (Position position : ship.getAllPositions()) {
            if (!isInField(position, size)) {
                return PlaceResult.DOESNT_FIT;
            }

            if (findShipByAnyPosition(field, position) != null) {
                return PlaceResult.ALREADY_A_SHIP;
            }

            if (findShipByNearPosition(field, position) != null) {
                return PlaceResult.TOO_CLOSE;
            }

        }
        return PlaceResult.SUCCESS;
    }

    public boolean isInField(@NotNull Position position, int fieldSize) {
        return position.getX() > 0 && position.getX() < fieldSize
               && position.getY() > 0 && position.getY() < fieldSize;
    }

    public boolean canHit(@NotNull OpponentField field, @NotNull Position position) {
        return field.getStateAt(position) == ShipState.EMPTY;
    }

    @Data
    public static final class PlaceResult {
        public static final PlaceResult SUCCESS = new PlaceResult("You can place a ship to this position", true);
        public static final PlaceResult DOESNT_FIT = new PlaceResult("Doesn't fit in the field", false);
        public static final PlaceResult ALREADY_A_SHIP = new PlaceResult("Impossible to place a ship on another", false);
        public static final PlaceResult TOO_CLOSE = new PlaceResult("Too close to another ship", false);

        private final String message;
        private final boolean success;
    }

    @NotNull
    public BattleShip insertionToShip(@NotNull BattleShipInsertion insertion) {
        return converter.insertionToShip(insertion);
    }

    public boolean isDefeated(@NotNull SelfField field) {
        return field.getLeftShips().isEmpty();
    }
}
