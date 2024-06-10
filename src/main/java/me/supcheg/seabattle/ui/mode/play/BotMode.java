package me.supcheg.seabattle.ui.mode.play;

import me.supcheg.seabattle.bot.BotMoveCalculator;
import me.supcheg.seabattle.field.OpponentField;
import me.supcheg.seabattle.field.SelfField;
import me.supcheg.seabattle.field.SelfFieldGenerator;
import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.net.packet.HelloPacket;
import me.supcheg.seabattle.net.packet.Packet;
import me.supcheg.seabattle.net.packet.PlayerMovePacket;
import me.supcheg.seabattle.net.packet.PlayerMoveResponsePacket;
import me.supcheg.seabattle.net.packet.ReadyForBattlePacket;
import me.supcheg.seabattle.ship.BattleShipInsertion;
import me.supcheg.seabattle.ship.Position;
import me.supcheg.seabattle.ship.ShipState;
import me.supcheg.seabattle.ui.Terminal;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static me.supcheg.seabattle.util.Unchecked.uncheckedCast;

public final class BotMode extends PlayMode {
    private SelfField botField;
    private OpponentField playerField;
    private BotMoveCalculator calculator;

    public BotMode(@NotNull Terminal terminal) {
        super(terminal);
    }

    @NotNull
    @Override
    protected NetworkController setupNetworkController() {
        return new BotNetworkController();
    }

    private class BotNetworkController implements NetworkController {

        private final Map<Class<? extends Packet>, List<Consumer<? extends Packet>>> listeners = new HashMap<>();
        private final Deque<Packet> queue = new LinkedList<>();

        @Override
        public void sendPacket(@NotNull Packet packet) {
            queue.add(packet);
        }

        @Override
        public void awaitResponse() {
            switch (Objects.requireNonNull(queue.poll())) {
                case HelloPacket ignored -> sendToPlayer(new HelloPacket("really_bot"));
                case ReadyForBattlePacket ignored -> {
                    initializeFields();
                    sendToPlayer(new ReadyForBattlePacket(botField));
                }
                case PlayerMovePacket move -> {
                    Position position = move.getTargetPosition();
                    ShipState result = fieldController.acceptPlayerMove(botField, position);
                    sendToPlayer(new PlayerMoveResponsePacket(position, result));
                    makeMove();
                }
                case PlayerMoveResponsePacket moveResponse -> {
                    ShipState state = moveResponse.getState();
                    Position position = moveResponse.getPosition();
                    fieldController.acceptPlayerMove(playerField, position, state);
                }
                default -> {
                }
            }
        }

        private void makeMove() {
            sendToPlayer(new PlayerMovePacket(calculator.calculateMove()));
        }

        private void sendToPlayer(@NotNull Packet packet) {
            listeners.getOrDefault(packet.getClass(), List.of())
                    .forEach(consumer -> consumer.accept(uncheckedCast(packet)));
        }

        @Override
        public <T extends Packet> void subscribeToPacket(@NotNull Class<T> packetType, @NotNull Consumer<T> listener) {
            listeners.computeIfAbsent(packetType, __ -> new ArrayList<>()).add(listener);
        }

        @Override
        public boolean isHost() {
            return true;
        }
    }

    private void initializeFields() {
        List<BattleShipInsertion> insertions = new SelfFieldGenerator(configuration, fieldController).generateInsertions();
        botField = new SelfField(configuration.getFieldSize());
        insertions.stream()
                .map(fieldController::insertionToShip)
                .forEach(ship -> botField.getLeftShips().add(ship));
        playerField = new OpponentField(configuration.getFieldSize());
        calculator = new BotMoveCalculator(playerField, fieldController);
    }
}
