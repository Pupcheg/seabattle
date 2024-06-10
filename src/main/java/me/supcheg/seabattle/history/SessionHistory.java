package me.supcheg.seabattle.history;

import lombok.Data;
import me.supcheg.seabattle.ship.Position;
import me.supcheg.seabattle.field.SelfField;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public final class SessionHistory {
    private final String selfUsername;
    private final String opponentUsername;
    private final SelfField originalSelfField;
    private final SelfField originalOpponentField;
    private final List<Position> selfMovesHistory = new ArrayList<>();
    private final List<Position> opponentMovesHistory = new ArrayList<>();

    private final LocalDateTime startTime;
    private LocalDateTime endTime;
}
