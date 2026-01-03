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

    @RabbitListener(queues = "events.processed")
    public void processProcessedEvent(EventDto eventDto) {
        log.info("Mongo: получено событие {}", eventDto.getUuid());

        EventDocument doc = new EventDocument();

        doc.setUuid(eventDto.getUuid());
        doc.setEventTime(eventDto.getEventTime());
        doc.setSqlSavedAt(eventDto.getSqlSavedAt());
        doc.setMongoSavedAt(LocalDateTime.now());

        mongoRepository.save(doc);

        log.info("Mongo: сохранено в архив {}", doc.getUuid());
    }
}