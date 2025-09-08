package org.apache.coyote.http11;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HttpCookie {

    private final Map<String, String> cookies;

    private HttpCookie(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public static HttpCookie from(String cookieHeader) {
        Map<String, String> cookiePairs = new HashMap<>();
        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=");
            if (keyValue.length == 2) {
                cookiePairs.put(keyValue[0], keyValue[1]);
            }
        }
        return new HttpCookie(cookiePairs);
    }

    public static String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    public static Optional<String> getSessionId(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return Optional.empty();
        }
        HttpCookie cookie = HttpCookie.from(cookieHeader);
        return cookie.getValue("JSESSIONID");
    }

    private Optional<String> getValue(String key) {
        return Optional.ofNullable(cookies.get(key));
    }
}
