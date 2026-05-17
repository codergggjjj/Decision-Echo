package com.exam.exam_backed.common;

public final class ErrorCode {
    public static final int SUCCESS = 0;
    public static final int PARAM_ERROR = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int CAPTCHA_ERROR = 1001;
    public static final int LOGIN_ERROR = 1002;
    public static final int SYSTEM_ERROR = 500;

    private ErrorCode() {
    }
}
