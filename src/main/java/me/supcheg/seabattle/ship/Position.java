package me.supcheg.seabattle.ship;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public final class Position {
    private final int x;
    private final int y;

    @NotNull
    public static Position fromString(@NotNull String raw) {
        int x = Character.toUpperCase(raw.charAt(0)) - 'A';
        int y = Integer.parseInt(raw.substring(1)) - 1;
        return new Position(x, y);
    }

    @Override
    @NotNull
    public String toString() {
        return Character.toString('A' + x) + (y + 1);
    }
}
