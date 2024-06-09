package me.supcheg.seabattle;

import com.sun.source.doctree.SeeTree;
import lombok.Data;

import java.util.Set;

@Data
public final class BattleField {
    public static final int SIZE = 16;

    private final Set<BattleShip> leftShips;
}
