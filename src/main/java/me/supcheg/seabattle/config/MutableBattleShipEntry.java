package me.supcheg.seabattle.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public final class MutableBattleShipEntry {
    private int length;
    private int amount;

    public MutableBattleShipEntry(@NotNull BattleShipEntry entry) {
        this(entry.getLength(), entry.getAmount());
    }

    public void subtractAmount() {
        amount--;
    }

    public void incrementAmount() {
        amount++;
    }
}