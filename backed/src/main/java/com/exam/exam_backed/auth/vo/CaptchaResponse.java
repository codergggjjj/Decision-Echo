package com.exam.exam_backed.auth.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record CaptchaResponse(String captchaId, String imageBase64, @JsonIgnore String answerForTest) {
}
