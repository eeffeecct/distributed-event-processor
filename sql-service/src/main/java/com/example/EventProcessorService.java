package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventProcessorService {

    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;

    // чтение очереди
    @RabbitListener(queues = "events.raw")
    @Transactional
    public void processEvent(EventDto event){
        log.info("Получено событие: {}", event.getUuid());

        int rows = eventRepository.save(event);

        if (rows == 0) {
            log.warn("Дубликат события: {}. Пропускаем.", event.getUuid());
            return;
        }

        event.setSqlSavedAt(LocalDateTime.now());

        rabbitTemplate.convertAndSend("events.processed", event); // отправка во вторую очередь к mongo
        log.info("Отправлено в event.processed: {}", event.getUuid());
    }
}
