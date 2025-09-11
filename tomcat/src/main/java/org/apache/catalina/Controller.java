package org.apache.catalina;

import org.apache.coyote.http11.message.HttpRequest;
import org.apache.coyote.http11.message.HttpResponse;

public interface Controller {
    HttpResponse service(HttpRequest request) throws Exception;
}
