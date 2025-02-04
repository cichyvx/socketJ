package com.github.cichyvx.socket;

import com.github.cichyvx.controller.Response;
import com.github.cichyvx.mapper.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class HttpResponder {

    private static final Logger log = LoggerFactory.getLogger(HttpResponder.class);
    private final ObjectMapper objectMapper;

    public static final String HTTP_OUTPUT = """
            HTTP/1.1 %s %s\r
            Content-Type: application/json\r
            Server: SocketJ\r
            Accept-Ranges: bytes\r
            Content-Length: %s\r
            Vary: Accept-Encoding\r
            Connection: %s\r
            \r
            %s""";

    public HttpResponder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void send(Response<?> response, OutputStream outputStream, boolean keep) {
        try {
            String responseJson = objectMapper.toJson(response.getData());
            DataOutputStream dos = new DataOutputStream(outputStream);
            String finalResponseData =
                    HTTP_OUTPUT.formatted(response.getStatus(), response.getMessage(), responseJson.length(), keep ? "keep-alive" : "keep "
                            , responseJson);
            dos.write(finalResponseData.getBytes(StandardCharsets.UTF_8));
            dos.flush();
        } catch (Exception e) {
            log.error("error on sending response", e);
        }
    }

}
