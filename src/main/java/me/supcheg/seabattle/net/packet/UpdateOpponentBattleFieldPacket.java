package me.supcheg.seabattle.net.packet;

import lombok.Data;
import me.supcheg.seabattle.BattleField;

@Data
public final class UpdateOpponentBattleFieldPacket implements Packet {
    private final BattleField field;
}
