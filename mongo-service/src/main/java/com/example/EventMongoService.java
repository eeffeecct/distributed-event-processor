package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventMongoService {

    private final EventMongoRepository mongoRepository;

    @RabbitListener(queues = RabbitQueueConstants.QUEUE_PROCESSED_EVENTS)
    public void processProcessedEvent(MongoEventMessage message) {
        log.debug("Mongo: получено событие {}", message.getUuid());

        EventDocument doc = createDocument(message);

        mongoRepository.save(doc);

        log.info("Mongo: сохранено в архив {}", doc.getUuid());
    }

    private EventDocument createDocument(MongoEventMessage message) {
        return EventDocument.builder()
                .uuid(message.getUuid())
                .eventTime(message.getEventTime())
                .sqlSavedAt(message.getSqlSavedAt())
                .mongoSavedAt(LocalDateTime.now())
                .build();
    }
}