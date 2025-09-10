package org.apache.coyote.controller;

import java.io.IOException;
import org.apache.coyote.http11.message.HttpRequest;
import org.apache.coyote.http11.message.HttpResponse;
import org.apache.coyote.util.ResourceLoader;

public class DefaultController extends AbstractController {

    @Override
    protected HttpResponse doPost(HttpRequest request) throws IOException {
        return HttpResponse.notFound(ResourceLoader.readWithFallback("/404.html"));
    }

    @Override
    protected HttpResponse doGet(HttpRequest request) throws IOException {
        String path = request.getRequestLine().getPath();
        try {
            return HttpResponse.ok(path, ResourceLoader.readWithFallback(path));
        } catch (Exception e) {
            return HttpResponse.notFound(ResourceLoader.readWithFallback("/404.html"));
        }
    }
}
