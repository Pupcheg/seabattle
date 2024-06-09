package me.supcheg.seabattle;

import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.net.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class SelfNetworkController implements NetworkController {
    private final Map<Class<? extends Packet>, List<Consumer<? extends Packet>>> selfType2listeners = new HashMap<>();
    private final Map<Class<? extends Packet>, List<Consumer<? extends Packet>>> opponentType2listeners = new HashMap<>();

    @Override
    public void sendPacket(@NotNull Packet packet) {
        List<Consumer<Packet>> consumers = (List<Consumer<Packet>>) (Object) opponentType2listeners.get(packet.getClass());
        consumers.forEach(consumer -> consumer.accept(packet));
    }

    @NotNull
    public NetworkController createOpponent() {
        return new NetworkController() {
            @Override
            public void sendPacket(@NotNull Packet packet) {
                List<Consumer<Packet>> consumers = (List<Consumer<Packet>>) (Object) selfType2listeners.get(packet.getClass());
                consumers.forEach(consumer -> consumer.accept(packet));
            }

            @Override
            public <T extends Packet> void subscribeToPacket(@NotNull Class<T> packetType, @NotNull Consumer<T> listener) {
                opponentType2listeners.computeIfAbsent(packetType, __ -> new ArrayList<>()).add(listener);
            }
        };
    }

    @Override
    public <T extends Packet> void subscribeToPacket(@NotNull Class<T> packetType, @NotNull Consumer<T> listener) {
        selfType2listeners.computeIfAbsent(packetType, __ -> new ArrayList<>()).add(listener);
    }
}
