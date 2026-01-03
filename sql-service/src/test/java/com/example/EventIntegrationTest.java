package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

/*
    Integration test:
        1. Send message to RabbitMQ
        2. Wait for read and save
        3. Check if event is in PostgreSQL
 */

@SpringBootTest
@Testcontainers
class EventIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withInitScript("schema.sql");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);

        registry.add("spring.rabbitmq.listener.simple.missing-queues-fatal", () -> "false");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldSaveEvent_whenMessageReceivedFromRabbit() {
        String uuid = UUID.randomUUID().toString();
        EventDto event = new EventDto();
        event.setUuid(uuid);
        event.setEventTime(LocalDateTime.now());

        rabbitTemplate.convertAndSend("events.raw", event);

        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> assertThat(eventRepository.findByUuid(uuid))
                    .isPresent()
                    .get()
                    .extracting(EventDto::getUuid)
                    .isEqualTo(uuid));
    }
}
