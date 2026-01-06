package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventCollectorService {

    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${external.events-url}")
    private String apiUrl;

    @Scheduled(fixedRate = 300_000)
    public void collectEvents() {
        log.info("---Загрузка ивентов---");

        try {
            EventDto[] events = restTemplate.getForObject(apiUrl, EventDto[].class); // дефолт массив ивентов

            if (events == null || events.length == 0) {
                log.info("Событий от API не получено");
                return;
            }

            log.info("Получено событий: {}. Начинаем отправку в RabbitMQ...", events.length);

            Arrays.stream(events)
                  .filter(Objects::nonNull)
                  .forEach(this::sendToRabbit);

            log.info("Все события успешно отправлены");

        } catch (Exception e) {
            log.error("Ошибка при получении данных: {}", e.getMessage());
        }
    }

    private void sendToRabbit(EventDto event) {
        log.debug("Отправка события UUID: {}", event.getUuid());
        rabbitTemplate.convertAndSend(RabbitQueueConstants.QUEUE_RAW_EVENTS, event);
    }
}