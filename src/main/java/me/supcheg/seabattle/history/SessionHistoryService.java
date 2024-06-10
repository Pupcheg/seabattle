package me.supcheg.seabattle.history;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SessionHistoryService {
    public void saveSessionHistory(@NotNull SessionHistory history) {
        Path dir = Path.of("/");

        Path savePath;
        do {
            savePath = dir.resolve(System.currentTimeMillis() + ".json");
        } while (Files.exists(savePath));


        try (BufferedWriter writer = Files.newBufferedWriter(savePath)) {
            new Gson().toJson(history, writer);
        } catch (Exception ignored) {
        }

    }
}
