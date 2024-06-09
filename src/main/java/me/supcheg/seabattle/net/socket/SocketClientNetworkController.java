package me.supcheg.seabattle.net.socket;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public final class SocketClientNetworkController extends SocketNetworkController {
    private final Socket socket;

    public SocketClientNetworkController() {
        this.socket = new Socket();
    }

    public void connect(@NotNull SocketAddress address) throws IOException {
        socket.connect(address);
    }

    @NotNull
    @Override
    protected InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @NotNull
    @Override
    protected OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }
}
