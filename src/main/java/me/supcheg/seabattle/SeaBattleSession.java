package me.supcheg.seabattle;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.net.packet.UpdateOpponentBattleFieldPacket;
import me.supcheg.seabattle.net.packet.PlayerMovePacket;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

@Data
@RequiredArgsConstructor
public final class SeaBattleSession {
    private final NetworkController networkController;
    private final BattleFieldController battleFieldController;

    private final BattleField selfField = new BattleField(new HashSet<>());
    private final BattleField opponentField = new BattleField(new HashSet<>());

    public void initialize() {
        networkController.subscribeToPacket(UpdateOpponentBattleFieldPacket.class, packet -> {
            opponentField.getLeftShips().addAll(packet.getField().getLeftShips());
        });
        networkController.subscribeToPacket(PlayerMovePacket.class, packet -> {
            battleFieldController.acceptPlayerMove(selfField, packet.getTargetPosition());
        });
    }

    public void placeSelfShip(@NotNull BattleShip ship) {
        selfField.getLeftShips().add(ship);
    }

    public void sendSelfField() {
        networkController.sendPacket(new UpdateOpponentBattleFieldPacket(selfField));
    }

    public void hitOn(@NotNull Position position) {
        networkController.sendPacket(new PlayerMovePacket(position));
        battleFieldController.acceptPlayerMove(opponentField, position);
    }
}
