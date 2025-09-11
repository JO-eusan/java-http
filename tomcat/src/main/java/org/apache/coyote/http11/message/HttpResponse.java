package org.apache.coyote.http11.message;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import org.apache.coyote.http11.StatusCode;

public class HttpResponse {

    private String protocol = "HTTP/1.1";
    private final StatusCode statusCode;
    private final Headers headers;
    private final String body;

    private HttpResponse(StatusCode statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = Headers.of(new LinkedHashMap<>());
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
        response.addHeader("Content-Length", String.valueOf(body.getBytes().length));
        return response;
    }

    private static String extractContentType(String requestURI) {
        if (requestURI.contains(".css")) {
            return "text/css";
        }
        return "text/html";
    }

    public void addHeader(String key, String value) {
        headers.add(key, value);
    }

    public String toMessage() {
        String headerString = headers.asMap().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue() + " ")
                .collect(Collectors.joining("\r\n"));
        return String.join("\r\n",
                protocol + " " + statusCode.getCode() + " " + statusCode.getText() + " ",
                headerString,
                "",
                body);
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
