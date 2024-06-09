package me.supcheg.seabattle.net.packet;

import lombok.Data;
import me.supcheg.seabattle.Position;

@Data
public final class PlayerMovePacket implements Packet {
    private final Position targetPosition;
}
