package com.github.cichyvx.controller;

public interface Controller<R extends Request> {

    Response<?> process(R request);

    String getPath();

    String getMethod();

}
