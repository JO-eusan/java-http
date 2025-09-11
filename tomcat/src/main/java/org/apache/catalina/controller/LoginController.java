package org.apache.catalina.controller;

import static org.reflections.Reflections.log;

import com.techcourse.model.User;
import com.techcourse.service.AuthService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.catalina.session.Session;
import org.apache.catalina.session.SessionManager;
import org.apache.coyote.http11.HttpCookie;
import org.apache.coyote.http11.message.HttpRequest;
import org.apache.coyote.http11.message.HttpResponse;
import org.apache.catalina.util.ResourceLoader;

public class LoginController extends AbstractController {

    @Override
    protected HttpResponse doPost(HttpRequest request) throws Exception {
        Map<String, String> bodyParams = parseRequestBody(request.getBody());
        try {
            User user = AuthService.login(
                    bodyParams.get("account"),
                    bodyParams.get("password")
            );
            log.info(user.toString());
            return createCookie(user);
        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패: {}", e.getMessage());
            return HttpResponse.redirect("/401.html");
        }
    }

    @Override
    protected HttpResponse doGet(HttpRequest request) throws Exception {
        String path = request.getRequestLine().getPath();
        return request.getHeaders().get("Cookie")
                .flatMap(HttpCookie::getSessionId)
                .map(SessionManager.getInstance()::findSession)
                .filter(session -> session.getAttribute("user") != null)
                .map(session -> HttpResponse.redirect("/index.html"))
                .orElse(HttpResponse.ok(path, ResourceLoader.readWithFallback(path)));
    }

    private Map<String, String> parseRequestBody(Optional<String> body) {
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

    private HttpResponse createCookie(User user) {
        String sessionId = HttpCookie.generateSessionId();
        Session session = new Session(sessionId);
        session.setAttribute("user", user);
        SessionManager.getInstance().add(session);

        HttpResponse response = HttpResponse.redirect("/index.html");
        response.addHeader("Set-Cookie", "JSESSIONID=" + sessionId);
        return response;
    }
}
