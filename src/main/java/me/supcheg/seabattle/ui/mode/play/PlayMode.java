package me.supcheg.seabattle.ui.mode.play;

import lombok.SneakyThrows;
import me.supcheg.seabattle.BattleFieldService;
import me.supcheg.seabattle.BattleShipInsertionConverter;
import me.supcheg.seabattle.Position;
import me.supcheg.seabattle.SelfField;
import me.supcheg.seabattle.config.BattleShipEntry;
import me.supcheg.seabattle.config.SeaBattleConfiguration;
import me.supcheg.seabattle.history.SessionHistoryService;
import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.session.SeaBattleSession;
import me.supcheg.seabattle.ui.BattleFieldCreation;
import me.supcheg.seabattle.ui.BattleFieldRenderer;
import me.supcheg.seabattle.ui.Terminal;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PlayMode implements Runnable {
    protected final BattleFieldService fieldController;
    protected final Terminal terminal;
    protected final BattleFieldRenderer renderer;
    protected final SeaBattleConfiguration configuration;

    protected PlayMode(@NotNull Terminal terminal) {
        this.fieldController = new BattleFieldService(
                new BattleShipInsertionConverter()
        );
        this.renderer = new BattleFieldRenderer(fieldController);
        this.terminal = terminal;
        configuration = new SeaBattleConfiguration(
                16,
                List.of(
                        new BattleShipEntry(6, 1),
                        new BattleShipEntry(5, 2),
                        new BattleShipEntry(4, 3),
                        new BattleShipEntry(3, 4),
                        new BattleShipEntry(2, 5),
                        new BattleShipEntry(1, 6)
                )
        );
    }

    @SneakyThrows
    @Override
    public void run() {
        String username = terminal.nextString("Enter username:");

        NetworkController networkController = setupNetworkController();

        SeaBattleSession session = new SeaBattleSession(
                configuration,
                networkController,
                fieldController,
                new SessionHistoryService()
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
        }
        while (!fieldController.isDefeated(session.getSelfField())) {
            makeMove(session);
            networkController.awaitResponse();

            renderFields(session);
            terminal.println("Your last move: " + session.getHistory().getSelfMovesHistory().getLast());
            terminal.println("Opponent move!");
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
            terminal.println("Last opponent move: " + opponentMovesHistory.getLast());
        }
        terminal.println("Your move!");

        Position position;
        while (true) {
            try {
                String raw = terminal.nextString("Enter a cell position:");
                position = Position.fromString(raw);
            } catch (Exception e) {
                terminal.println("Wrong syntax!");
                continue;
            }

            if (!fieldController.isInField(position, configuration.getFieldSize())) {
                terminal.println("It isn't a valid position!");
                continue;
            }

            if (!fieldController.canHit(session.getOpponentField(), position)) {
                terminal.println("You can't hit on this cell!");
                continue;
            }

            break;
        }

        session.sendMoveAt(position);
    }


    private void renderFields(@NotNull SeaBattleSession session) {
        terminal.clear();
        renderer.renderField(session.getSelfField(), terminal.getOut());
        terminal.println();
        renderer.renderField(session.getOpponentField(), terminal.getOut());
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
    protected abstract NetworkController setupNetworkController() throws IOException;
}
