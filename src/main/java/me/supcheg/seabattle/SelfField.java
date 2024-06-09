package me.supcheg.seabattle;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public final class SelfField {
    public SelfField(int size) {
        this(new HashSet<>(), size);
    }

    private final Set<BattleShip> leftShips;
    private final int size;
}
