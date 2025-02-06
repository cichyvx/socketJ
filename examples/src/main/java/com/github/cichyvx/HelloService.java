package com.github.cichyvx;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public HelloWorldResponse sayHello(HelloWorldRequest request) {
        return new HelloWorldResponse("Hello %s".formatted(request.name()));
    }

}
