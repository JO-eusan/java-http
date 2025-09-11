package org.apache.coyote.http11.message;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestLine {

    private final HttpMethod method;
    private final String path;
    private final String protocol;
    private final Map<String, String> queryParams;

    private RequestLine(HttpMethod method, String path, String protocol, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.queryParams = queryParams;
    }

    public static RequestLine from(String startLine) {
        if (startLine == null || startLine.isBlank()) {
            throw new IllegalArgumentException("빈 요청 라인이 전달되었습니다.");
        }

        String[] parts = startLine.split(" ", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException("잘못된 요청 라인: " + startLine);
        }
        String method = parts[0];
        String uri = parts[1];
        String protocol = parts[2];

        int queryIndex = uri.indexOf("?");
        String path = extractPath(uri, queryIndex);
        Map<String, String> queryParams = extractQueryParams(uri, queryIndex);

        return new RequestLine(HttpMethod.from(method), path, protocol, queryParams);
    }

    private static String extractPath(String uri, int queryIndex) {
        if (queryIndex == -1) {
            return uri;
        }
        return uri.substring(0, queryIndex);
    }

    private static Map<String, String> extractQueryParams(String uri, int queryIndex) {
        if (queryIndex == -1) {
            return Collections.emptyMap();
        }
        String queryString = uri.substring(queryIndex + 1);
        return parseQueryParams(queryString);
    }

    private static Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> result = new HashMap<>();
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = keyValue.length == 2
                    ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                    : "";
            result.put(key, value);
        }
        return result;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public Optional<String> getQueryParam(String key) {
        return Optional.ofNullable(queryParams.get(key));
    }
}
