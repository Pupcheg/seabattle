package me.supcheg.seabattle.net.packet;

import lombok.Data;
import me.supcheg.seabattle.SelfField;

@Data
public final class ReadyForBattlePacket implements Packet {
    private final SelfField opponentView;
}
