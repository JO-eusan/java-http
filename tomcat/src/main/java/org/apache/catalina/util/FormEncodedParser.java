package org.apache.catalina.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FormEncodedParser {

    private FormEncodedParser() {
    }

    public static Map<String, String> parse(Optional<String> body) {
        Map<String, String> params = new HashMap<>();
        if (body.isPresent()) {
            for (String param : body.get().split("&")) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                } else if (keyValue.length == 1) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    params.put(key, "");
                }
            }
        }
        return params;
    }
}
