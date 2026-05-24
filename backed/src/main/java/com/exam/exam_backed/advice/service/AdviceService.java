package com.exam.exam_backed.advice.service;

import com.exam.exam_backed.advice.dto.AdviceGenerateRequest;
import com.exam.exam_backed.advice.vo.DecisionAdviceResponse;

public interface AdviceService {
    DecisionAdviceResponse generate(AdviceGenerateRequest request, Long userId);
}
