package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        EventDto eventDto = new EventDto();
        eventDto.setUuid("mongo-test-uuid");
        eventDto.setEventTime(LocalDateTime.now());
        eventDto.setSqlSavedAt(LocalDateTime.now().minusSeconds(1));

        processor.processProcessedEvent(eventDto);

        ArgumentCaptor<EventDocument> captor = ArgumentCaptor.forClass(EventDocument.class);

        verify(mongoRepository, times(1)).save(captor.capture());

        EventDocument savedDoc = captor.getValue();
        assertEquals("mongo-test-uuid", savedDoc.getUuid());
        assertEquals(eventDto.getSqlSavedAt(), savedDoc.getSqlSavedAt());

        assert(savedDoc.getMongoSavedAt() != null);
    }

    @Test
    void processProcessedEvent_shouldThrowException_whenMongoFails() {
        EventDto eventDto = new EventDto();
        eventDto.setUuid("error-uuid");

        doThrow(new RuntimeException("Mongo connection lost"))
                .when(mongoRepository).save(any(EventDocument.class));

        assertThrows(RuntimeException.class, () -> {
            processor.processProcessedEvent(eventDto);
        });

        verify(mongoRepository, times(1)).save(any(EventDocument.class));
    }
}