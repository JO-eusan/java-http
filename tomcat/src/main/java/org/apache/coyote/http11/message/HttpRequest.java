package org.apache.coyote.http11.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {

    private final RequestLine requestLine;
    private final Headers headers;
    private final String body;

    private HttpRequest(RequestLine requestLine, Headers headers, String body) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest of(String request) {
        String[] parts = request.split("\r\n\r\n", 2);
        String headerPart = parts[0];
        String bodyPart = parts.length > 1
                ? parts[1]
                : null;

        String[] lines = headerPart.split("\r\n");
        RequestLine requestLine = RequestLine.from(lines[0]);

        Map<String, String> headerLines = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            System.out.println(line);
            int idx = line.indexOf(":");
            if (idx != -1) {
                headerLines.put(line.substring(0, idx).trim(), line.substring(idx + 1).trim());
            }
        }

        return new HttpRequest(requestLine, Headers.of(headerLines), bodyPart);
    }

    public RequestLine getRequestLine() {
        return requestLine;
    }

    public Headers getHeaders() {
        return headers;
    }

    public Optional<String> getBody() {
        return Optional.ofNullable(body);
    }
}
