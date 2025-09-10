package org.apache.coyote.controller;

import org.apache.coyote.http11.message.HttpRequest;
import org.apache.coyote.http11.message.HttpResponse;

public abstract class AbstractController implements Controller {

    @Override
    public HttpResponse service(HttpRequest request) throws Exception {
        switch (request.getRequestLine().getMethod()) {
            case "GET" -> {
                return doGet(request);
            }
            case "POST" -> {
                return doPost(request);
            }
            default -> {
                return HttpResponse.notFound("지원하지 않는 메서드입니다.");
            }
        }
    }

    protected HttpResponse doGet(HttpRequest request) throws Exception {
        return HttpResponse.notFound("GET method not implemented");
    }

    protected HttpResponse doPost(HttpRequest request) throws Exception {
        return HttpResponse.notFound("POST method not implemented");
    }
}
