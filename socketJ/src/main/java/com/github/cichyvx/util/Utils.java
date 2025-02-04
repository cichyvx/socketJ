package com.github.cichyvx.util;

public class Utils {

    public static void sneakySleep() {
        try {
            Thread.sleep(1L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
