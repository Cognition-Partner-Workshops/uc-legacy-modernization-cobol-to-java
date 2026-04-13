package com.carddemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CardDemoApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring Boot application context loads successfully
    }
}
