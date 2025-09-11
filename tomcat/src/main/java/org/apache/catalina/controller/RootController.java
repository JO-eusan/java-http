package org.apache.catalina.controller;

import org.apache.coyote.http11.message.HttpRequest;
import org.apache.coyote.http11.message.HttpResponse;

public class RootController extends AbstractController {

    @Override
    public HttpResponse doGet(HttpRequest request) {
        return HttpResponse.ok(request.getRequestLine().getPath(), "Hello world!");
    }
}
