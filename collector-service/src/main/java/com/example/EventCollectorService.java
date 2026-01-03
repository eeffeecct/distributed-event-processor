package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventCollectorService {
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${external.events-url}")
    private String apiUrl;

    @Scheduled(fixedRate = 300000) // каждые 5 мин. 5000мс = 5сек
    public void collectEvents() {
        log.info("---Загрузка ивентов---");

        try {
            // JSON to EventDto.class for validation
            EventDto[] events = restTemplate.getForObject(apiUrl, EventDto[].class); // дефолт массив ивентов

            if (events == null || events.length == 0) {
                log.info("Нет событий");
                return;
            }

            for (EventDto event : events) {
                log.info("Отправляем событие: {}", event);

                // EventDto.class to JSON
                rabbitTemplate.convertAndSend("events.raw", event);
            }
        } catch (Exception e) {
            log.error("Ошибка при получении данных: {}", e.getMessage());
        }
    }
}
