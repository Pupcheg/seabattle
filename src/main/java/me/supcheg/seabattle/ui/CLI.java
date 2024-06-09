package me.supcheg.seabattle.ui;

import lombok.RequiredArgsConstructor;
import me.supcheg.seabattle.BattleFieldController;
import me.supcheg.seabattle.SeaBattleSession;
import me.supcheg.seabattle.net.socket.SocketClientNetworkController;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

@RequiredArgsConstructor
public final class CLI {

    private final InputStream in;
    private final PrintStream out;

    public static void main(String[] args) {
        new CLI(System.in, System.out).run();
    }

    public void run() {
        Scanner scanner = new Scanner(in);

        out.println("Enter username:");
        String username = scanner.nextLine();

        SocketClientNetworkController networkController = new SocketClientNetworkController();
        SeaBattleSession session = new SeaBattleSession(
                networkController,
                new BattleFieldController()
        );
        session.initialize();
    }
}
