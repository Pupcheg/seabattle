package me.supcheg.seabattle.net.socket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import lombok.Data;
import lombok.SneakyThrows;
import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.net.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static me.supcheg.seabattle.Unchecked.uncheckedCast;

public abstract class SocketNetworkController implements NetworkController {
    protected final Map<Class<? extends Packet>, List<Consumer<? extends Packet>>> listeners;
    protected final Gson gson;

    protected SocketNetworkController() {
        this.listeners = new HashMap<>();
        this.gson = new Gson();
    }

    @SneakyThrows
    @Override
    public void sendPacket(@NotNull Packet packet) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(getOutputStream()));
        gson.toJson(
                new TypedPacket(
                        packet.getClass().getName(),
                        packet
                ),
                TypedPacket.class,
                writer
        );
        writer.flush();
    }

    @SneakyThrows
    @Override
    public void awaitResponse() {
        JsonObject jsonObject = gson.fromJson(new JsonReader(new InputStreamReader(getInputStream())), JsonObject.class);
        String type = jsonObject.get("type").getAsString();
        Class<?> clazz = Class.forName(type);
        Object packet = gson.fromJson(jsonObject.get("packet"), clazz);
        listeners.getOrDefault(clazz, List.of()).forEach(consumer -> consumer.accept(uncheckedCast(packet)));
    }

    @Override
    public <T extends Packet> void subscribeToPacket(@NotNull Class<T> packetType, @NotNull Consumer<T> listener) {
        listeners.computeIfAbsent(packetType, __ -> new ArrayList<>()).add(listener);
    }

    @NotNull
    protected abstract InputStream getInputStream() throws IOException;

    @NotNull
    protected abstract OutputStream getOutputStream() throws IOException;

    @Data
    private static final class TypedPacket {
        private final String type;
        private final Packet packet;
    }
}
