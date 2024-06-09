package me.supcheg.seabattle.ui;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Scanner;

@Data
public final class Terminal {
    @Delegate
    private final Scanner scanner;
    @Delegate
    private final PrintStream out;

    @NotNull
    public <E extends Enum<E>> E nextEnum(@NotNull Class<E> type, @NotNull String inputMessage) {
        while (true) {
            out.println(inputMessage);
            String raw = scanner.next();
            try {
                return Enum.valueOf(type, raw.toUpperCase());
            } catch (Exception ignored) {
                out.println("Invalid value");
            }
        }
    }

    public int nextInt(@NotNull String inputMessage) {
        while (true) {
            out.println(inputMessage);
            try {
                return scanner.nextInt();
            } catch (Exception ignored) {
                out.println("Invalid value");
            }
        }
    }

    @NotNull
    public String nextString(@NotNull String inputMessage) {
        out.println(inputMessage);
        return scanner.next();
    }

    @NotNull
    public String nextNonBlankLine() {
        String value;
        do {
            value = scanner.nextLine();
        } while (value.isBlank());
        return value;
    }

    public void pause() {
        out.println("Press any button to continue...");
        scanner.nextLine();
    }

    @SneakyThrows
    public void clear() {
        if (System.getProperty("os.name").contains("Windows")) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            out.print("\033[H\033[2J");
            out.flush();
        }
    }
}
