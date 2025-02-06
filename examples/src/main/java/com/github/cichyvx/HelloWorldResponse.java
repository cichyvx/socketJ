package com.github.cichyvx;

import com.github.cichyvx.controller.Response;

public record HelloWorldResponse(String message) implements Response<HelloWorldResponse> {

    @Override
    public int getStatus() {
        return Response.OK_CODE;
    }

    @Override
    public HelloWorldResponse getData() {
        return this;
    }

    @Override
    public String getMessage() {
        return Response.OK_MSG;
    }
}
