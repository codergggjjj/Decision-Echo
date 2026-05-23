package com.exam.exam_backed.advice.service;

import com.exam.exam_backed.advice.vo.DecisionAdviceResponse;
import com.exam.exam_backed.decision.dto.DecisionCreateRequest;

public interface AdviceService {
    DecisionAdviceResponse generate(DecisionCreateRequest request);
}
