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

    @Transactional
    @RabbitListener(queues = RabbitQueueConstants.QUEUE_RAW_EVENTS)
    public void processEvent(EventDto incomingEvent) {
        log.debug("Получено событие: {}", incomingEvent.getUuid());

        EventEntity entity = EventEntity.builder()  // из EntityDto в EventEntity для SQL
                .uuid(incomingEvent.getUuid())
                .eventTime(incomingEvent.getEventTime())
                .build();

        int rowsInserted = eventRepository.save(entity);

        if (rowsInserted == 0) {
            log.warn("Дубликат события в Postgres: {}. Пропускаем отправку в Mongo.", incomingEvent.getUuid());
            return;
        }

        sendToAnalytics(incomingEvent);
    }

    private void sendToAnalytics(EventDto savedEvent) {
        LocalDateTime processedAt = LocalDateTime.now();    // Время сохранения в PostgreSQL

        MongoEventMessage messageForMongo = new MongoEventMessage(
                savedEvent.getUuid(),
                savedEvent.getEventTime(),
                processedAt
        );

        rabbitTemplate.convertAndSend(RabbitQueueConstants.QUEUE_PROCESSED_EVENTS, messageForMongo);

        log.info("Событие {} сохранено в SQL в {} и отправлено в RabbitMQ",
                savedEvent.getUuid(), processedAt);
    }
}