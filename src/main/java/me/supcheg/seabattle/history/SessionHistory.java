package me.supcheg.seabattle.history;

import lombok.Data;
import me.supcheg.seabattle.Position;
import me.supcheg.seabattle.SelfField;

import java.util.ArrayList;
import java.util.List;

@Data
public final class SessionHistory {
    private final SelfField originalSelfField;
    private final SelfField originalOpponentField;
    private final List<Position> selfMovesHistory = new ArrayList<>();
    private final List<Position> opponentMovesHistory = new ArrayList<>();
}
