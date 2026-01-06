package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventMongoServiceTest {

    @Mock
    private EventMongoRepository mongoRepository;

    @InjectMocks
    private EventMongoService processor;

    @Test
    void processProcessedEvent_shouldSaveToMongo_whenValidEvent() {
        MongoEventMessage message = MongoEventMessage.builder()
                .uuid("mongo-test-uuid")
                .eventTime(LocalDateTime.now())
                .sqlSavedAt(LocalDateTime.now().minusSeconds(1))
                .build();

        processor.processProcessedEvent(message);

        ArgumentCaptor<EventDocument> captor = ArgumentCaptor.forClass(EventDocument.class);
        verify(mongoRepository, times(1)).save(captor.capture());

        EventDocument savedDoc = captor.getValue();

        assertEquals("mongo-test-uuid", savedDoc.getUuid());
        assertEquals(message.getSqlSavedAt(), savedDoc.getSqlSavedAt());
        assertEquals(message.getEventTime(), savedDoc.getEventTime());

        assertNotNull(savedDoc.getMongoSavedAt());
    }

    @Test
    void processProcessedEvent_shouldThrowException_whenMongoFails() {
        MongoEventMessage message = MongoEventMessage.builder()
                .uuid("error-uuid")
                .build();

        doThrow(new RuntimeException("Mongo connection lost"))
                .when(mongoRepository).save(any(EventDocument.class));

        assertThrows(RuntimeException.class, () -> {
            processor.processProcessedEvent(message);
        });

        verify(mongoRepository, times(1)).save(any(EventDocument.class));
    }
}