package me.supcheg.seabattle.net.packet;

import lombok.Data;
import me.supcheg.seabattle.Position;
import me.supcheg.seabattle.ShipState;

@Data
public final class PlayerMoveResponsePacket implements Packet {
    private final Position position;
    private final ShipState state;
}
