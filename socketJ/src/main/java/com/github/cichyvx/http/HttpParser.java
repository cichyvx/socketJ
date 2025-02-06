package com.github.cichyvx.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpParser {

    private final static String CONTENT_LENGTH = "Content-Length";
    private static final Logger log = LoggerFactory.getLogger(HttpParser.class);

    public HttpRequest parseSocket(InputStream httpInputStream) {
        try {
            log.trace("Parsing HTTP request");
            StringBuilder method = new StringBuilder();
            StringBuilder path = new StringBuilder();
            StringBuilder httpVersion = new StringBuilder();

            Map<String, String> headers = new HashMap<>();
            StringBuilder headerNameString = new StringBuilder();
            StringBuilder headerValueString = new StringBuilder();

            int bodyCount = 0;
            int contentLength = 0;

            StringBuilder body = new StringBuilder();

            var inputStream = new DataInputStream(httpInputStream);
            boolean httpInfoSection = true;
            boolean methodSection = true;
            boolean pathSection = false;
            boolean httpVersionSection = false;

            boolean headerSection = false;
            boolean headerName = true;
            boolean headerValue = false;

            boolean betweenHeaderBodySection = false;
            boolean bodySection = false;

            Character lastChar = null;
            while (inputStream.available() > 0) {
                char read;

                try {
                    read = (char) inputStream.read();
                } catch (EOFException ex) {
                    log.warn("EOF while reading HTTP request");
                    break;
                }

                if (read == '\r') {
                    if (bodySection) {
                        bodyCount++;
                    }
                    continue;
                }

                if (read == '\uFFFF') {
                    log.warn("unexpected null value while reading HTTP request");
                    return null;
                }

                /* FIRST LINE - HTTP METADATA INFO*/
                if (httpInfoSection) {

                    /* METHOD */
                    if (methodSection) {
                        if (isSpace(read)) {
                            methodSection = false;
                            pathSection = true;
                        } else {
                            method.append(read);
                        }
                        /* PATH */
                    } else if (pathSection) {
                        if (isSpace(read)) {
                            pathSection = false;
                            httpVersionSection = true;
                        } else {
                            path.append(read);
                        }
                        /* HTTP VERSION */
                    } else if (httpVersionSection) {
                        if (isEndOfLine(read)) {
                            httpVersionSection = false;
                            httpInfoSection = false;
                            headerSection = true;
                        } else {
                            httpVersion.append(read);
                        }
                    }
                    /* HEADERS */
                } else if (headerSection) {
                    if (isEndOfLine(read) && isEndOfLine(lastChar)) {
                        headerSection = false;
                        betweenHeaderBodySection = true;
                    } else if (headerName) {
                        if (read == ':') {
                            headerName = false;
                            headerValue = true;
                        } else {
                            headerNameString.append(read);
                        }
                    } else if (headerValue) {
                        if (isEndOfLine(read)) {
                            headerValue = false;
                            headerName = true;

                            headers.put(headerNameString.toString(), headerValueString.substring(1));
                            headerNameString = new StringBuilder();
                            headerValueString = new StringBuilder();
                        } else {
                            headerValueString.append(read);
                        }
                    }
                    /* BETWEEN HEADERS AND BODY */
                } else if (betweenHeaderBodySection) {
                    if (read != '\r' && read != '\n') {
                        betweenHeaderBodySection = false;
                        bodySection = true;
                        contentLength = Integer.parseInt(headers.get(CONTENT_LENGTH).replace(" ", ""));
                        body.append(read);
                        bodyCount++;
                    }
                } else if (bodySection) {
                    body.append(read);
                    bodyCount++;

                    if (bodyCount >= contentLength) {
                        break;
                    }
                }

                lastChar = read;

            }

            return new HttpRequest(method.toString(), path.toString(), httpVersion.toString(), headers, body.toString());
        } catch (IOException e) {
            throw new HttpParsingException(e.getMessage());
        }
    }

    private static boolean isEndOfLine(char read) {
        return read == '\n';
    }

    private boolean isSpace(char read) {
        return read == ' ';
    }

}
