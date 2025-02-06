package com.github.cichyvx;

import com.github.cichyvx.controller.Request;

public record HelloWorldRequest(String name) implements Request {
}
