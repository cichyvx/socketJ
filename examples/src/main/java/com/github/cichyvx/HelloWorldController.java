package com.github.cichyvx;

import com.github.cichyvx.controller.Controller;

@org.springframework.stereotype.Controller
public class HelloWorldController implements Controller<HelloWorldRequest> {

    private final HelloService helloService;

    public HelloWorldController(HelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    public HelloWorldResponse process(HelloWorldRequest request) {
        return helloService.sayHello(request);
    }

    @Override
    public String getPath() {
        return "/api/v1/hello";
    }

    @Override
    public String getMethod() {
        return "GET";
    }
}
