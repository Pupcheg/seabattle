package me.supcheg.seabattle.net.packet;

import lombok.Data;
import me.supcheg.seabattle.field.SelfField;

@Data
public final class ReadyForBattlePacket implements Packet {
    private final SelfField opponentView;
}
