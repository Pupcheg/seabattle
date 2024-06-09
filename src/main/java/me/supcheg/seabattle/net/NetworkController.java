package me.supcheg.seabattle.net;

import me.supcheg.seabattle.net.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface NetworkController {

    void sendPacket(@NotNull Packet packet);

    void awaitResponse();

    <T extends Packet> void subscribeToPacket(@NotNull Class<T> packetType, @NotNull Consumer<T> listener);

    boolean isHost();
}
