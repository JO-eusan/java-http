package org.apache.catalina.controller;

import static org.reflections.Reflections.log;

import com.techcourse.model.User;
import com.techcourse.service.AuthService;
import java.io.IOException;
import java.util.Map;
import org.apache.catalina.session.Session;
import org.apache.catalina.session.SessionManager;
import org.apache.catalina.util.FormEncodedParser;
import org.apache.catalina.util.ResourceLoader;
import org.apache.coyote.http11.HttpCookie;
import org.apache.coyote.http11.message.HttpRequest;
import org.apache.coyote.http11.message.HttpResponse;

public class RegisterController extends AbstractController {

    @Override
    public HttpResponse doPost(HttpRequest request) {
        Map<String, String> bodyParams = FormEncodedParser.parse(request.getBody());
        try {
            User user = AuthService.register(
                    bodyParams.get("account"),
                    bodyParams.get("password"),
                    bodyParams.get("email")
            );
            log.info(user.toString());
            return createCookie(user);
        } catch (Exception e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return HttpResponse.redirect("/401.html");
        }
    }

    @Override
    public HttpResponse doGet(HttpRequest request) throws IOException {
        String path = request.getRequestLine().getPath();
        return HttpResponse.ok(path, ResourceLoader.readWithFallback(path));
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
