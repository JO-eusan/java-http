package org.apache.coyote.http11;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpResponse {

    private final StatusCode statusCode;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final String body;

    private HttpResponse(StatusCode statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public static HttpResponse ok(String requestURI, String body) {
        HttpResponse response = new HttpResponse(StatusCode.OK, body);
        response.addHeader("Content-Type", extractContentType(requestURI) + ";charset=utf-8");
        response.addHeader("Content-Length", String.valueOf(body.getBytes().length));
        return response;
    }

    public static HttpResponse redirect(String location) {
        HttpResponse response = new HttpResponse(StatusCode.FOUND, "");
        response.addHeader("Location", location);
        response.addHeader("Content-Type", "text/html;charset=utf-8");
        response.addHeader("Content-Length", "0");
        return response;
    }

    public static HttpResponse notFound(String body) {
        HttpResponse response = new HttpResponse(StatusCode.NOT_FOUND, body);
        response.addHeader("Content-Type", "text/html;charset=utf-8");
        response.addHeader("Content-Length", "0");
        return response;
    }

    private static String extractContentType(String requestURI) {
        if (requestURI.contains(".css")) {
            return "text/css";
        }
        return "text/html";
    }

    private void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String toHttpMessage() {
        String headerString = headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + " ")
                .collect(Collectors.joining("\r\n"));
        return "HTTP/1.1 " + statusCode.getStatusCode() + " \r\n" +
                headerString + "\r\n\r\n" +
                body;
    }
}
