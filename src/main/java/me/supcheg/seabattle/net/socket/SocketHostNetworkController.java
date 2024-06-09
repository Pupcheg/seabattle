package me.supcheg.seabattle.net.socket;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public final class SocketHostNetworkController extends SocketNetworkController {

    private final ServerSocket serverSocket;
    private Socket connection;

    public SocketHostNetworkController(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void awaitForConnection() throws IOException {
        this.connection = serverSocket.accept();
    }

    @NotNull
    @Override
    protected InputStream getInputStream() throws IOException {
        return connection.getInputStream();
    }

    @NotNull
    @Override
    protected OutputStream getOutputStream() throws IOException {
        return connection.getOutputStream();
    }
}
