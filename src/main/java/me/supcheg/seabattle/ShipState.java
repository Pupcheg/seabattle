package me.supcheg.seabattle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipState {
    ALIVE('X'), CLOSE('-'), HIT('x'), DEATH('D'), EMPTY('~');

    private final char rendered;
}