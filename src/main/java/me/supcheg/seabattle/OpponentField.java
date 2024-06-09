package me.supcheg.seabattle;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

@EqualsAndHashCode
public final class OpponentField {
    private final ShipState[][] knownMap;

    public OpponentField(int size) {
        this.knownMap = new ShipState[size][size];
        for (ShipState[] states : knownMap) {
            Arrays.fill(states, ShipState.EMPTY);
        }
    }

    @NotNull
    public ShipState getStateAt(@NotNull Position position) {
        return knownMap[position.getX()][position.getY()];
    }

    public void setStateAt(@NotNull Position position, @NotNull ShipState state) {
        Objects.requireNonNull(state, "state");
        knownMap[position.getX()][position.getY()] = state;
    }

    public int getSize() {
        return knownMap.length;
    }
}
