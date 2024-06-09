package me.supcheg.seabattle.config;

import lombok.Data;

import java.util.List;

@Data
public final class SeaBattleConfiguration {
    private final int fieldSize;
    private final List<BattleShipEntry> shipEntries;
}
