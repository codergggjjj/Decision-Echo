package com.exam.exam_backed.auth.service;

import com.exam.exam_backed.auth.vo.CaptchaResponse;

public interface CaptchaService {
    CaptchaResponse createCaptcha();

    void validate(String captchaId, String captchaCode);
}
