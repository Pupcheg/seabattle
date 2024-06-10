package me.supcheg.seabattle.ui.mode.play;

import me.supcheg.seabattle.net.NetworkController;
import me.supcheg.seabattle.net.socket.SocketClientNetworkController;
import me.supcheg.seabattle.net.socket.SocketHostNetworkController;
import me.supcheg.seabattle.ui.Terminal;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class OnlineMode extends PlayMode {

    public OnlineMode(@NotNull Terminal terminal) {
        super(terminal);
    }

    @NotNull
    @Override
    protected NetworkController setupNetworkController() throws IOException {
        String message = "Which mode do you want to use?";
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
