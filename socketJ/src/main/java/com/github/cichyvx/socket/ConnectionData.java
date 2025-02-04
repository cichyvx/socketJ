package com.github.cichyvx.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionData {

    private final Socket socket;

    public ConnectionData(Socket socket) {
        this.socket = socket;
    }

    public boolean isDataAvailable() {
        try {
            return socket.getInputStream().available() > 0;
        } catch (IOException e) {
            throw new UnexpectedSocketException(e);
        }
    }

    public InputStream getInputStream() {
        try {
            return socket.getInputStream();
        } catch (IOException ex) {
            throw new UnexpectedSocketException(ex);
        }
    }

    public OutputStream getOutputStream() {
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
            throw new UnexpectedSocketException(e);
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new UnexpectedSocketException(e);
        }
    }
}
