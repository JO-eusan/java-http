package org.apache.coyote.http11.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public static HttpRequest of(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String startLine = reader.readLine();
        if (startLine == null || startLine.isBlank()) {
            throw new IOException("빈 요청이 수신되었습니다.");
        }

        Map<String, String> headers = new HashMap<>();
        for (String headerLine; (headerLine = reader.readLine()) != null && !headerLine.isEmpty(); ) {
            int colonIndex = headerLine.indexOf(":");
            if (colonIndex != -1) {
                headers.put(
                        headerLine.substring(0, colonIndex).trim(),
                        headerLine.substring(colonIndex + 1).trim()
                );
            }
        }

        String body = null;
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] buffer = new char[contentLength];
            int readCount = reader.read(buffer, 0, contentLength);
            if (readCount > 0) {
                body = new String(buffer, 0, readCount);
            }
        }
        return new HttpRequest(RequestLine.from(startLine), Headers.of(headers), body);
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
