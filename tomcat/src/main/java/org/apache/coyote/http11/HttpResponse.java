package org.apache.coyote.http11;

import java.util.HashMap;
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

    public static HttpResponse from(StatusCode statusCode, String requestURI, String resource) {
        HttpResponse response = new HttpResponse(statusCode, resource);
        response.addHeader("Content-Type", extractContentType(requestURI) + ";charset=utf-8");
        response.addHeader("Content-Length", String.valueOf(resource.getBytes().length));
        return response;
    }

    public static HttpResponse redirect(String location) {
        HttpResponse response = new HttpResponse(StatusCode.FOUND, "");
        response.addHeader("Location", location);
        response.addHeader("Content-Type", "text/html;charset=utf-8");
        response.addHeader("Content-Length", "0");
        return response;
    }

    private void addHeader(String key, String value) {
        headers.put(key, value);
    }

    private static String extractContentType(String requestURI) {
        if (requestURI.contains(".css")) {
            return "text/css";
        }
        return "text/html";
    }

    @Override
    public String toString() {
        String headerString = headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + " ")
                .collect(Collectors.joining("\r\n"));

        return "HTTP/1.1 " + statusCode.getStatusCode() + " \r\n" +
                headerString + "\r\n\r\n" +
                body;
    }
}
