package me.supcheg.seabattle.ui;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.ui.mode.EditorMode;
import me.supcheg.seabattle.ui.mode.play.OnlineMode;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Function;

@Data
public final class CLI implements Runnable {
    private final Terminal terminal;

    public static void main(@NotNull String @NotNull [] args) {
        new CLI(System.in, System.out).run();
    }

    public CLI(@NotNull InputStream in, @NotNull PrintStream out) {
        this.terminal = new Terminal(new Scanner(in), out);
    }

    @Override
    public void run() {
        terminal.nextEnum(Mode.class, "Enter mode:").constructor.apply(terminal).run();
    }

    @RequiredArgsConstructor
    private enum Mode {
        ONLINE(OnlineMode::new),
        BOT(__ -> () -> System.out.println("Not implemented")),
        EDITOR(EditorMode::new),
        HISTORY(__ -> () -> System.out.println("Not implemented"));

        private final Function<Terminal, Runnable> constructor;
    }

}
