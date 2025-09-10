package org.apache.coyote.http11.message;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class Headers {

    private final Map<String, String> headers;

    private Headers(Map<String, String> headers) {
        this.headers = headers;
    }

    public static Headers of(Map<String, String> headers) {
        return new Headers(headers);
    }

    public void add(String key, String value) {
        headers.put(key, value);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(headers.get(key));
    }

    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(headers);
    }
}
