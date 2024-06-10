package me.supcheg.seabattle.session;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.field.BattleFieldService;
import me.supcheg.seabattle.field.OpponentField;
import me.supcheg.seabattle.ship.Position;
import me.supcheg.seabattle.field.SelfField;
import me.supcheg.seabattle.ship.ShipState;
import me.supcheg.seabattle.config.SeaBattleConfiguration;
import me.supcheg.seabattle.history.SessionHistory;
import me.supcheg.seabattle.history.SessionHistoryService;
import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.net.packet.HelloPacket;
import me.supcheg.seabattle.net.packet.PlayerMovePacket;
import me.supcheg.seabattle.net.packet.PlayerMoveResponsePacket;
import me.supcheg.seabattle.net.packet.ReadyForBattlePacket;
import me.supcheg.seabattle.net.packet.SelfFieldDefeatedPacket;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public final class SeaBattleSession {

    private final SeaBattleConfiguration config;

    private final NetworkController networkController;
    private final BattleFieldService battleFieldService;

    private final SeaBattleSessionMeta meta;

    private final SelfField selfField;
    private final OpponentField opponentField;

    private SessionHistory history;
    private final SessionHistoryService historyService;

    public SeaBattleSession(@NotNull SeaBattleConfiguration config,
                            @NotNull NetworkController networkController,
                            @NotNull BattleFieldService battleFieldService,
                            @NotNull SessionHistoryService historyService) {
        this.config = config;
        this.networkController = networkController;
        this.battleFieldService = battleFieldService;

        this.meta = new SeaBattleSessionMeta();
        this.selfField = new SelfField(config.getFieldSize());
        this.opponentField = new OpponentField(config.getFieldSize());
        this.historyService = historyService;
    }

    public void subscribeSetupPackets() {
        networkController.subscribeToPacket(
                HelloPacket.class,
                packet -> meta.setOpponentUsername(packet.getUsername())
        );
        networkController.subscribeToPacket(ReadyForBattlePacket.class, packet -> {
            history = new SessionHistory(
                    meta.getSelfUsername(),
                    meta.getOpponentUsername(),
                    selfField,
                    packet.getOpponentView(),
                    LocalDateTime.now()
            );
        });
    }

    public void subscribeGamePackets() {
        networkController.subscribeToPacket(
                PlayerMovePacket.class,
                packet -> {
                    Position targetPosition = packet.getTargetPosition();
                    history.getOpponentMovesHistory().add(targetPosition);
                    ShipState result = battleFieldService.acceptPlayerMove(selfField, targetPosition);
                    networkController.sendPacket(new PlayerMoveResponsePacket(targetPosition, result));
                }
        );
        networkController.subscribeToPacket(
                PlayerMoveResponsePacket.class,
                packet -> battleFieldService.acceptPlayerMove(opponentField, packet.getPosition(), packet.getState())
        );
        networkController.subscribeToPacket(
                SelfFieldDefeatedPacket.class,
                packet -> saveSessionHistory()
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
        saveSessionHistory();
        networkController.sendPacket(new SelfFieldDefeatedPacket());
    }

    public void saveSessionHistory() {
        LocalDateTime now = LocalDateTime.now();
        history.setEndTime(now);
        historyService.saveSessionHistory(history);
    }
}
