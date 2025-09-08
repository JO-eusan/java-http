package org.apache.coyote.http11;

import com.techcourse.exception.UncheckedServletException;
import com.techcourse.model.User;
import com.techcourse.service.AuthService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.coyote.Processor;
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

            HttpRequest request = extractHttpRequest(inputStream);
            HttpResponse response = createHttpResponse(request);

            outputStream.write(response.toHttpMessage().getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private HttpRequest extractHttpRequest(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String startLine = reader.readLine();
        String[] parts = startLine.split(" ");
        String method = parts[0];
        String uri = parts[1];

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                String headerName = line.substring(0, colonIndex).trim();
                String headerValue = line.substring(colonIndex + 1).trim();
                headers.put(headerName, headerValue);
            }
        }

        String body = null;
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] bodyChars = new char[contentLength];
            int readCount = reader.read(bodyChars, 0, contentLength);
            if (readCount > 0) {
                body = new String(bodyChars, 0, readCount);
            }
        }

        return HttpRequest.from(method, uri, body);
    }

    private HttpResponse createHttpResponse(HttpRequest request) throws IOException {
        String requestURI = request.getURI();
        String requestMethod = request.getMethod();

        if (requestURI.equals("/")) {
            return HttpResponse.ok(requestURI, "Hello world!");
        }

        if (requestURI.equals("/login") || requestURI.equals("/register")) {
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
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
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
        HttpResponse response = HttpResponse.redirect("/index.html");
        response.addHeader("Set-Cookie", "JSESSIONID=" + sessionId);
        return response;
    }
}
