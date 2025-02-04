package com.github.cichyvx.controller.mock;

import com.github.cichyvx.controller.Controller;
import com.github.cichyvx.controller.Response;

@org.springframework.stereotype.Controller
public class MockController implements Controller<MockRequest> {
    @Override
    public Response<?> process(MockRequest request) {
        return new Response<>() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public Object getData() {
                return request;
            }

            @Override
            public String getMessage() {
                return Response.OK_MSG;
            }
        };
    }

    @Override
    public String getPath() {
        return "/hello";
    }

    @Override
    public String getMethod() {
        return "GET";
    }
}
