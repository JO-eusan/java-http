package org.apache.coyote.http11;

public enum StatusCode {
    OK(200),
    FOUND(302),
    ;

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public String getStatusCode() {
        return String.join(" ", String.valueOf(code), this.name());
    }
}
