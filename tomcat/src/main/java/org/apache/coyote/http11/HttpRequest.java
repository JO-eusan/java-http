package org.apache.coyote.http11;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {

    private final String method;
    private final String URI;
    private final Map<String, String> queryParams;
    private final Optional<String> body;

    private HttpRequest(String method, String URI, Map<String, String> queryParams, Optional<String> body) {
        this.method = method;
        this.URI = URI;
        this.queryParams = queryParams;
        this.body = body;
    }

    public static HttpRequest from(String method, String URI, String body) {
        int queryIndex = URI.indexOf("?");
        if (queryIndex == -1) {
            return new HttpRequest(method, URI, Collections.emptyMap(), Optional.ofNullable(body));
        }
        String path = URI.substring(0, queryIndex);
        String queryString = URI.substring(queryIndex + 1);
        return new HttpRequest(method, path, parseQueryParams(queryString), Optional.ofNullable(body));
    }

    private static Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> result = new HashMap<>();
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }

    public String getMethod() {
        return method;
    }

    public String getURI() {
        return URI;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Optional<String> getBody() {
        return body;
    }
}
