package me.supcheg.seabattle;

import me.supcheg.seabattle.ui.BattleFieldRenderer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeaBattleSessionTest {
    @Test
    void run() throws IOException {
        SelfNetworkController networkController = new SelfNetworkController();
        SeaBattleSession firstSession = new SeaBattleSession(
                networkController,
                new BattleFieldController()
        );
        SeaBattleSession secondSession = new SeaBattleSession(
                networkController.createOpponent(),
                new BattleFieldController()
        );

        firstSession.initialize();
        secondSession.initialize();

        firstSession.placeSelfShip(new BattleShip(
                new ArrayList<>(List.of(new Position(0, 0)))
        ));
        firstSession.sendSelfField();
        secondSession.sendSelfField();

        secondSession.hitOn(new Position(0, 0));

        assertTrue(
                firstSession.getSelfField().getLeftShips().iterator().next()
                        .getAlivePositions().isEmpty()
        );
        assertTrue(
                secondSession.getOpponentField().getLeftShips().iterator().next()
                        .getAlivePositions().isEmpty()
        );

        firstSession.placeSelfShip(new BattleShip(new ArrayList<>(List.of(new Position(0, 1), new Position(1, 1)))));
        secondSession.hitOn(new Position(0, 1));

        PrintWriter out = new PrintWriter(System.out);
        new BattleFieldRenderer(new BattleFieldController())
                .renderSelfField(firstSession.getSelfField(), out);
        out.flush();
    }
}
