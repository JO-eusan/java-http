package org.apache.coyote.http11;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.StubSocket;

class Http11ProcessorTest {

    @Test
    @DisplayName("/ 요청 시 Hello World!를 반환한다.")
    void process() {
        // given
        final var socket = new StubSocket();
        final var processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        var expected = String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 12 ",
                "",
                "Hello world!");

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("/index.html 요청 시, 정적 파일을 읽어올 수 있다.")
    void index() throws IOException {
        // given
        final String httpRequest = String.join("\r\n",
                "GET /index.html HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/index.html");
        var expected = "HTTP/1.1 200 OK \r\n" +
                "Content-Type: text/html;charset=utf-8 \r\n" +
                "Content-Length: 5564 \r\n" +
                "\r\n" +
                new String(Files.readAllBytes(new File(resource.getFile()).toPath()));

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("로그인 성공 시 /index.html을 Location 헤더에 담아 리다이렉트한다.")
    void login_success() throws IOException {
        // given
        String loginRequestSuccess = String.join("\r\n",
                "GET /login?account=gugu&password=password HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        StubSocket socket = new StubSocket(loginRequestSuccess);
        Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        String successOutput = socket.output();
        System.out.println(successOutput);
        assertThat(successOutput).contains("HTTP/1.1 302 FOUND");
        assertThat(successOutput).contains("Location: /index.html");
    }

    @Test
    @DisplayName("로그인 실패 시 /404.html을 Location 헤더에 담아 리다이렉트한다.")
    void login_fail() throws IOException {
        // given
        String loginRequestSuccess = String.join("\r\n",
                "GET /login?account=gugu&password=fail_password HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        StubSocket socket = new StubSocket(loginRequestSuccess);
        Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        String successOutput = socket.output();
        System.out.println(successOutput);
        assertThat(successOutput).contains("HTTP/1.1 302 FOUND");
        assertThat(successOutput).contains("Location: /404.html");
    }
}
