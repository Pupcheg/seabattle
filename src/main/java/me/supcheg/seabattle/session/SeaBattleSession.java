package me.supcheg.seabattle.session;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.BattleFieldController;
import me.supcheg.seabattle.OpponentField;
import me.supcheg.seabattle.Position;
import me.supcheg.seabattle.SelfField;
import me.supcheg.seabattle.ShipState;
import me.supcheg.seabattle.config.SeaBattleConfiguration;
import me.supcheg.seabattle.history.SessionHistory;
import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.net.packet.HelloPacket;
import me.supcheg.seabattle.net.packet.PlayerMovePacket;
import me.supcheg.seabattle.net.packet.PlayerMoveResponsePacket;
import me.supcheg.seabattle.net.packet.ReadyForBattlePacket;
import me.supcheg.seabattle.net.packet.SelfFieldDefeatedPacket;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
public final class SeaBattleSession {

    private final SeaBattleConfiguration config;

    private final NetworkController networkController;
    private final BattleFieldController battleFieldController;

    private final SeaBattleSessionMeta meta;

    private final SelfField selfField;
    private final OpponentField opponentField;

    private SessionHistory history;

    public SeaBattleSession(@NotNull SeaBattleConfiguration config,
                            @NotNull NetworkController networkController,
                            @NotNull BattleFieldController battleFieldController) {
        this.config = config;
        this.networkController = networkController;
        this.battleFieldController = battleFieldController;

        this.meta = new SeaBattleSessionMeta();
        this.selfField = new SelfField(config.getFieldSize());
        this.opponentField = new OpponentField(config.getFieldSize());
    }

    public void subscribeSetupPackets() {
        networkController.subscribeToPacket(
                HelloPacket.class,
                packet -> meta.setOpponentUsername(packet.getUsername())
        );
        networkController.subscribeToPacket(ReadyForBattlePacket.class, packet -> {
            history = new SessionHistory(
                    selfField,
                    packet.getOpponentView()
            );
        });
    }

    public void subscribeGamePackets() {
        networkController.subscribeToPacket(
                PlayerMovePacket.class,
                packet -> {
                    Position targetPosition = packet.getTargetPosition();
                    history.getOpponentMovesHistory().add(targetPosition);
                    ShipState result = battleFieldController.acceptPlayerMove(selfField, targetPosition);
                    networkController.sendPacket(new PlayerMoveResponsePacket(targetPosition, result));
                }
        );
        networkController.subscribeToPacket(
                PlayerMoveResponsePacket.class,
                packet -> battleFieldController.acceptPlayerMove(opponentField, packet.getPosition(), packet.getState())
        );
    }

    public void onOpponentDefeated(@NotNull Runnable runnable) {
        networkController.subscribeToPacket(SelfFieldDefeatedPacket.class, __ -> runnable.run());
    }

    public void sendHello() {
        networkController.sendPacket(new HelloPacket(meta.getSelfUsername()));
    }

    public void sendReady() {
        networkController.sendPacket(new ReadyForBattlePacket(selfField));
    }

    public void sendMoveAt(@NotNull Position position) {
        history.getSelfMovesHistory().add(position);
        networkController.sendPacket(new PlayerMovePacket(position));
    }

    public void sendDefeated() {
        networkController.sendPacket(new SelfFieldDefeatedPacket());
    }
}
