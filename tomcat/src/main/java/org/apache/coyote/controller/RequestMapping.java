package org.apache.coyote.controller;

import java.util.HashMap;
import java.util.Map;
import org.apache.coyote.http11.message.HttpRequest;

public class RequestMapping {

    private static final Map<String, Controller> mappings = new HashMap<>();

    static {
        mappings.put("/", new RootController());
        mappings.put("/login", new LoginController());
        mappings.put("/register", new RegisterController());
    }

    public static Controller getController(HttpRequest request) {
        return mappings.getOrDefault(
                request.getRequestLine().getPath(),
                new DefaultController());
    }
}
