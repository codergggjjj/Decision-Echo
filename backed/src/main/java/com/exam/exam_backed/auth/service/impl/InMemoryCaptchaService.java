package com.exam.exam_backed.auth.service.impl;

import com.exam.exam_backed.auth.service.CaptchaService;
import com.exam.exam_backed.auth.vo.CaptchaResponse;
import com.exam.exam_backed.common.BusinessException;
import com.exam.exam_backed.common.ErrorCode;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryCaptchaService implements CaptchaService {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 42;
    private static final long CAPTCHA_TTL_SECONDS = 5 * 60;
    private final Random random = new Random();
    private final Map<String, CaptchaEntry> captchas = new ConcurrentHashMap<>();

    @Override
    public CaptchaResponse createCaptcha() {
        String answer = String.valueOf(1000 + random.nextInt(9000));
        String captchaId = "cap_" + UUID.randomUUID().toString().replace("-", "");
        captchas.put(captchaId, new CaptchaEntry(answer, Instant.now().plusSeconds(CAPTCHA_TTL_SECONDS)));
        return new CaptchaResponse(captchaId, createImage(answer), answer);
    }

    @Override
    public void validate(String captchaId, String captchaCode) {
        CaptchaEntry entry = captchas.remove(captchaId);
        if (entry == null) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "验证码不存在或已失效");
        }
        if (entry.expireAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "验证码已过期");
        }
        if (!entry.answer().equals(captchaCode == null ? "" : captchaCode.trim())) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR, "验证码错误");
        }
    }

    private String createImage(String answer) {
        try {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(244, 247, 251));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            graphics.setFont(new Font("Arial", Font.BOLD, 26));
            graphics.setColor(new Color(31, 41, 55));
            graphics.drawString(answer, 28, 30);
            graphics.setColor(new Color(80, 120, 180, 90));
            for (int i = 0; i < 6; i++) {
                graphics.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), random.nextInt(WIDTH), random.nextInt(HEIGHT));
            }
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码生成失败");
        }
    }

    private record CaptchaEntry(String answer, Instant expireAt) {
    }
}
