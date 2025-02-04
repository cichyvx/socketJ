package com.github.cichyvx;

import com.github.cichyvx.socket.SocketListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.github.cichyvx")
public class App {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(App.class);
    }

    public App(SocketListener socketListener) {
        socketListener.start();
    }
}
