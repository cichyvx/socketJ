package com.github.cichyvx.socket;

public class UnexpectedSocketException extends RuntimeException {
    public UnexpectedSocketException(Exception exception) {
        super(exception);
    }
}
