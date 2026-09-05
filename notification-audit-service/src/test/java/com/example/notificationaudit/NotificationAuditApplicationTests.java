package com.example.notificationaudit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.rabbitmq.host=localhost",
    "spring.mail.host=smtp.mailtrap.io"
})
class NotificationAuditApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context loads successfully
    }
}
