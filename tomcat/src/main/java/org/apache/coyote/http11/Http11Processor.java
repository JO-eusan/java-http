package org.apache.coyote.http11;

import com.techcourse.exception.UncheckedServletException;
import com.techcourse.model.User;
import com.techcourse.service.AuthService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.session.Session;
import org.apache.catalina.session.SessionManager;
import org.apache.coyote.Processor;
import org.apache.coyote.http11.message.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream()) {

            HttpRequest request = HttpRequest.of(readRequestMessage(inputStream));
            HttpResponse response = createHttpResponse(request);

            outputStream.write(response.toHttpMessage().getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String readRequestMessage(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private HttpResponse createHttpResponse(HttpRequest request) throws IOException {
        String requestURI = request.getRequestLine().getPath();
        String requestMethod = request.getRequestLine().getMethod();

        if (requestURI.equals("/")) {
            return HttpResponse.ok(requestURI, "Hello world!");
        }
        if (requestURI.equals("/login")) {
            if (requestMethod.equals("POST")) {
                return handleFormRequest(requestURI, requestMethod, request);
            }
            return request.getHeaders().get("Cookie")
                    .flatMap(HttpCookie::getSessionId)
                    .map(SessionManager.getInstance()::findSession)
                    .filter(session -> session != null && session.getAttribute("user") != null)
                    .map(session -> HttpResponse.redirect("/index.html"))
                    .orElseGet(() -> {
                        try {
                            return handleFormRequest(requestURI, requestMethod, request);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        if (requestURI.equals("/register")) {
            return handleFormRequest(requestURI, requestMethod, request);
        }
        return HttpResponse.ok(requestURI, readResourceWithFallback(requestURI));
    }

    private HttpResponse handleFormRequest(String path, String method, HttpRequest request) throws IOException {
        if (method.equals("GET")) {
            return HttpResponse.ok(path, readResourceWithFallback(path));
        }
        if (method.equals("POST")) {
            Map<String, String> bodyParams = request.getBody()
                    .map(this::parseRequestBody)
                    .orElse(Collections.emptyMap());
            return switch (path) {
                case "/login" -> login(bodyParams);
                case "/register" -> register(bodyParams);
                default -> HttpResponse.notFound(readResourceWithFallback("/404.html"));
            };
        }
        return HttpResponse.notFound(readResourceWithFallback("/404.html"));
    }

    private Map<String, String> parseRequestBody(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isBlank()) {
            return params;
        }
        for (String param : body.split("&")) {
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
        return params;
    }

    private String readResourceWithFallback(String path) throws IOException {
        try {
            return readResourceFile("static" + path);
        } catch (IOException e) {
            return readResourceFile("static" + path + ".html");
        }
    }

    private String readResourceFile(String path) throws IOException {
        URL resource = getClass().getClassLoader().getResource(path);
        if (resource == null) {
            throw new IOException("리소스를 찾을 수 없습니다: " + path);
        }
        Path filePath = new File(resource.getPath()).toPath();
        return Files.readString(filePath);
    }

    private HttpResponse login(Map<String, String> bodyParams) {
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

    private HttpResponse register(Map<String, String> bodyParams) {
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
            return HttpResponse.redirect("/404.html");
        }
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
