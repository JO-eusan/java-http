package org.apache.coyote.http11.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Headers {

    private final Map<String, String> headers;

    private Headers(Map<String, String> headers) {
        this.headers = new HashMap<>(headers);
    }

    public static Headers of(Map<String, String> headers) {
        return new Headers(headers);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(headers.get(key));
    }
}
