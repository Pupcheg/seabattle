package me.supcheg.seabattle.net.packet;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public final class HelloPacket implements Packet {
    private final String username;
}
