package com.github.cichyvx.socket;

import com.github.cichyvx.config.Config;
import com.github.cichyvx.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;

@Component
public class SocketListener {

    private static final Logger log = LoggerFactory.getLogger(SocketListener.class);
    private final Thread thread;
    private final ServerSocket serverSocket;
    private final ConnectionDispatcher connectionDispatcher;
    private static boolean running;

    public SocketListener(ConnectionDispatcher connectionDispatcher, Config config) {
        this.connectionDispatcher = connectionDispatcher;
        int port = config.getPort();
        this.thread = new Thread(this::context);

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void start() {
        if (running) {
            throw new IllegalStateException("Socket listener is already running");
        }
        running = true;
        thread.start();
    }

    private void context() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    log.debug("Accepted connection from: {}", socket.getRemoteSocketAddress());
                    connectionDispatcher.openNewConnection(socket);
                } else {
                    Utils.sneakySleep();
                }
            } catch (Exception e) {
                log.error("exception during socket accepting!", e);
            }
        }
    }

}
