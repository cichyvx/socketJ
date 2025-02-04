package com.github.cichyvx.mapper;

public class ParsingException extends RuntimeException {
    public ParsingException(String message) {
        super(message);
    }
    public ParsingException(Throwable cause) {
        super(cause);
    }
}
