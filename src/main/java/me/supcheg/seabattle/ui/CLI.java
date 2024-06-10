package me.supcheg.seabattle.ui;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.ui.mode.EditorMode;
import me.supcheg.seabattle.ui.mode.play.BotMode;
import me.supcheg.seabattle.ui.mode.play.HistoryMode;
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
        terminal.nextEnum(Mode.class, "Which mode do you want to run?").constructor.apply(terminal).run();
    }

    @RequiredArgsConstructor
    private enum Mode {
        ONLINE(OnlineMode::new),
        BOT(BotMode::new),
        EDITOR(EditorMode::new),
        HISTORY(HistoryMode::new);

        private final Function<Terminal, Runnable> constructor;
    }

}
