package com.github.cichyvx.socket;

import com.github.cichyvx.context.PathMapper;
import com.github.cichyvx.controller.Controller;
import com.github.cichyvx.controller.ControllerInvoker;
import com.github.cichyvx.controller.Response;
import com.github.cichyvx.http.HttpParser;
import com.github.cichyvx.http.HttpRequest;
import com.github.cichyvx.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class ConnectionDispatcher {

    private final static Queue<ConnectionData> connectionQueue = new ConcurrentLinkedQueue<>();
    private final static Executor executor = Executors.newFixedThreadPool(2);
    private final static Logger log = LoggerFactory.getLogger(ConnectionDispatcher.class);

    public ConnectionDispatcher(HttpParser httpParser, PathMapper pathMapper, ControllerInvoker controllerInvoker, HttpResponder responder) {
        new Thread(() -> {
            while (true) {
                var data = connectionQueue.poll();
                if (data != null) {

                    if (data.isDataAvailable()) {
                        Runnable r = () -> {
                            HttpRequest httpRequest = httpParser.parseSocket(data.getInputStream());
                            Controller<?> controller = pathMapper.getController(httpRequest.path(), httpRequest.method());
                            boolean reuseSocket = true;
                            if (controller != null) {
                                Response<?> response = controllerInvoker.invokeController(controller, httpRequest);

                                Map<String, String> headers = httpRequest.headers();
                                String connection = headers.get("Connection");

                                if (connection != null && !connection.equals("keep-alive")) {
                                    reuseSocket = false;
                                }

                                responder.send(response, data.getOutputStream(), reuseSocket);
                            }

                            if (reuseSocket) {
                                connectionQueue.offer(data);
                            } else {
                                data.close();
                            }
                        };

                        executor.execute(r);
                    } else {
                        connectionQueue.offer(data);
                    }

                } else {
                    Utils.sneakySleep();
                }
            }
        }).start();
    }

    void openNewConnection(Socket socket) {
        connectionQueue.add(new ConnectionData(socket));
    }

}
