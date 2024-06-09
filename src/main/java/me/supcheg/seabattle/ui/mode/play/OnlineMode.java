package me.supcheg.seabattle.ui.mode.play;

import lombok.SneakyThrows;
import me.supcheg.seabattle.BattleFieldController;
import me.supcheg.seabattle.BattleShipInsertionConverter;
import me.supcheg.seabattle.Position;
import me.supcheg.seabattle.SelfField;
import me.supcheg.seabattle.config.BattleShipEntry;
import me.supcheg.seabattle.config.SeaBattleConfiguration;
import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.net.socket.SocketClientNetworkController;
import me.supcheg.seabattle.net.socket.SocketHostNetworkController;
import me.supcheg.seabattle.session.SeaBattleSession;
import me.supcheg.seabattle.ui.BattleFieldCreation;
import me.supcheg.seabattle.ui.BattleFieldRenderer;
import me.supcheg.seabattle.ui.Terminal;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class OnlineMode implements Runnable {
    private final BattleFieldController fieldController;
    private final Terminal terminal;
    private final BattleFieldRenderer renderer;

    public OnlineMode(@NotNull Terminal terminal) {
        this.fieldController = new BattleFieldController(
                new BattleShipInsertionConverter()
        );
        this.renderer = new BattleFieldRenderer(fieldController);
        this.terminal = terminal;
    }

    @SneakyThrows
    @Override
    public void run() {
        String username = terminal.nextString("Enter username:");

        NetworkController networkController = setupNetworkController();

        SeaBattleSession session = new SeaBattleSession(
                new SeaBattleConfiguration(
                        16,
                        List.of(
                                new BattleShipEntry(6, 1),
                                new BattleShipEntry(5, 2),
                                new BattleShipEntry(4, 3),
                                new BattleShipEntry(3, 4),
                                new BattleShipEntry(2, 5),
                                new BattleShipEntry(1, 6)
                        )
                ),
                networkController,
                fieldController
        );

        session.getMeta().setSelfUsername(username);

        session.subscribeSetupPackets();
        session.sendHello();
        networkController.awaitResponse();

        terminal.printf("Opponent name: %s\n", session.getMeta().getSelfUsername());

        placeAllShips(session);
        session.sendReady();
        networkController.awaitResponse();

        session.subscribeGamePackets();
        AtomicBoolean opponentDefeated = new AtomicBoolean(false);
        session.onOpponentDefeated(() -> opponentDefeated.set(true));

        if (!networkController.isHost()) {
            renderFields(session);
            networkController.awaitResponse();
            networkController.awaitResponse();
        }
        while (!isDefeated(session.getSelfField())) {
            makeMove(session);
            networkController.awaitResponse();
            networkController.awaitResponse();

            if (opponentDefeated.get()) {
                terminal.println("Opponent defeated!");
                terminal.pause();
                return;
            }
        }
        session.sendDefeated();
        terminal.println("You defeated!");
        terminal.pause();
    }

    private void makeMove(@NotNull SeaBattleSession session) {
        renderFields(session);

        List<Position> opponentMovesHistory = session.getHistory().getOpponentMovesHistory();
        if (!opponentMovesHistory.isEmpty()) {
            terminal.println("Opponent last move: " + opponentMovesHistory.getLast());
        }
        terminal.println("Your move!");

        Position position;
        while (true) {
            try {
                String raw = terminal.nextString("Enter a cell position:");
                position = Position.fromString(raw);
                break;
            } catch (Exception e) {
                terminal.println("Invalid position");
            }
        }

        session.sendMoveAt(position);
        renderFields(session);

        terminal.println("Your last move: " + session.getHistory().getSelfMovesHistory().getLast());
        terminal.println("Opponent move!");
    }


    private void renderFields(@NotNull SeaBattleSession session) {
        terminal.clear();
        renderer.renderField(session.getSelfField(), terminal.getOut());
        terminal.println();
        renderer.renderField(session.getOpponentField(), terminal.getOut());
    }

    private boolean isDefeated(@NotNull SelfField field) {
        return field.getLeftShips().isEmpty();
    }

    private void placeAllShips(@NotNull SeaBattleSession session) {
        BattleFieldCreation battleFieldCreation = new BattleFieldCreation(
                BattleFieldCreation.FieldCreationMode.VALID_ONLY,
                renderer,
                session.getConfig(),
                fieldController,
                terminal
        );
        SelfField field = battleFieldCreation.makeField();
        session.getSelfField().getLeftShips().addAll(field.getLeftShips());
    }

    @NotNull
    private NetworkController setupNetworkController() throws IOException {
        String message = "Which mode do you want to use? Acceptable values: " +
                         Arrays.stream(NetworkControllerType.values())
                                 .map(Enum::name)
                                 .map(String::toLowerCase)
                                 .collect(Collectors.joining(", "));
        return switch (terminal.nextEnum(NetworkControllerType.class, message)) {
            case HOST -> {
                int port = terminal.nextInt("Enter connection port:");
                SocketHostNetworkController networkController = new SocketHostNetworkController(port);

                terminal.printf("Hosting server on %d\n", networkController.getPort());
                terminal.println("Waiting for connection...");
                networkController.awaitForConnection();

                yield networkController;
            }
            case CLIENT -> {
                while (true) {
                    String host = terminal.nextString("Enter hostname:");
                    int port = terminal.nextInt("Enter connection port:");
                    InetSocketAddress address = new InetSocketAddress(host, port);
                    SocketClientNetworkController networkController = new SocketClientNetworkController();
                    try {
                        networkController.connect(address);
                        yield networkController;
                    } catch (Exception e) {
                        terminal.printf("Unable to connect to %s:%d\n", host, port);
                    }
                }
            }
        };
    }

    private enum NetworkControllerType {
        HOST, CLIENT
    }
}
