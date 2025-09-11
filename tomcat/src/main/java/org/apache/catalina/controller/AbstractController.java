package org.apache.catalina.controller;

import java.io.IOException;
import org.apache.catalina.Controller;
import org.apache.coyote.http11.message.HttpRequest;
import org.apache.coyote.http11.message.HttpResponse;

public abstract class AbstractController implements Controller {

    @Override
    public HttpResponse service(HttpRequest request) {
        return request.getRequestLine().getMethod().handle(this, request);
    }

    public HttpResponse doGet(HttpRequest request) throws IOException {
        return HttpResponse.notFound("GET method not implemented");
    }

    public HttpResponse doPost(HttpRequest request) throws IOException {
        return HttpResponse.notFound("POST method not implemented");
    }
}
