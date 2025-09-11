package org.apache.catalina.util;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceLoader {

    private ResourceLoader() {
    }

    public static String readWithFallback(String path) throws IOException {
        try {
            return read("static" + path);
        } catch (IOException e) {
            return read("static" + path + ".html");
        }
    }

    private static String read(String path) throws IOException {
        URL resource = ResourceLoader.class.getClassLoader().getResource(path);
        if (resource == null) {
            throw new IOException("리소스를 찾을 수 없습니다: " + path);
        }
        Path filePath = Path.of(resource.getPath());
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }
}
