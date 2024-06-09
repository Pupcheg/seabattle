package me.supcheg.seabattle;

import me.supcheg.seabattle.ui.CLI;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

class SeaBattleSessionTest {
    @Test
    void run() {
        CompletableFuture<?> host = runCLI("""
                host
                890
                me1
                """
        );
        CompletableFuture<?> client = runCLI("""
                client
                localhost
                889
                localhost
                890
                me2
                """
        );

        host.join();
        client.join();
    }

    private CompletableFuture<?> runCLI(@NotNull String commands) {
        return CompletableFuture.runAsync(new CLI(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)), System.out));
    }
}
