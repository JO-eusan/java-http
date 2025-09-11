package org.apache.coyote.http11.message;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiFunction;
import org.apache.catalina.controller.AbstractController;

public enum HttpMethod {
    GET((controller, request) -> {
        try {
            return controller.doGet(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }),
    POST((controller, request) -> {
        try {
            return controller.doPost(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

    private final BiFunction<AbstractController, HttpRequest, HttpResponse> handler;

    HttpMethod(BiFunction<AbstractController, HttpRequest, HttpResponse> handler) {
        this.handler = handler;
    }

    public static HttpMethod from(String method) {
        return Arrays.stream(values())
                .filter(m -> m.name().equalsIgnoreCase(method))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 메서드: " + method));
    }

    public HttpResponse handle(AbstractController controller, HttpRequest request) {
        return handler.apply(controller, request);
    }
}
