package me.supcheg.seabattle.util;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public final class Unchecked {
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T uncheckedCast(Object o) {
        return (T) o;
    }
}
