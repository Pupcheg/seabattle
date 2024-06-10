package me.supcheg.seabattle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipState {
    ALIVE('X', true),
    CLOSE('-', true),
    HIT('x', false),
    DEATH('D', false),
    EMPTY('~', true);

    private final char rendered;
    private final boolean endOfMove;
}