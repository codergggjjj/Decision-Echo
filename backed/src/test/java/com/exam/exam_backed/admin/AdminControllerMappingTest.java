package com.exam.exam_backed.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AdminControllerMappingTest {
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Test
    void resetPasswordEndpointIsRegistered() {
        boolean exists = requestMappingHandlerMapping.getHandlerMethods()
                .keySet()
                .stream()
                .map(RequestMappingInfo::toString)
                .anyMatch(mapping -> mapping.contains("PUT [/api/admin/users/{id}/password/reset]"));

        assertTrue(exists);
    }
}
