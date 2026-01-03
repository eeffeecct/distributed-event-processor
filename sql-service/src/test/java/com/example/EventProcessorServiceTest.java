package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventProcessorServiceTest {

    @Mock
    private EventRepository repository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private EventProcessorService service;

    @Test
    void processEvent_shouldSaveAndSend_whenNewEvent() {
        EventDto incomingEvent = new EventDto();
        incomingEvent.setUuid("test-uuid-new");

        when(repository.save(incomingEvent)).thenReturn(1);

        service.processEvent(incomingEvent);

        verify(repository, times(1)).save(incomingEvent);

        verify(rabbitTemplate, times(1))
            .convertAndSend(eq("events.processed"), eq(incomingEvent));
    }

    @Test
    void processEvent_shouldIgnore_whenDuplicate() {
        EventDto incomingEvent = new EventDto();
        incomingEvent.setUuid("test-uuid-duplicate");

        when(repository.save(incomingEvent)).thenReturn(0);

        service.processEvent(incomingEvent);

        verify(repository, times(1)).save(incomingEvent);

        // проверка что ничего не отправлено в rabbit
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void processEvent_shouldThrowException_whenDbFails() {
        EventDto incomingEvent = new EventDto();
        incomingEvent.setUuid("test-uuid-sql-error");

        doThrow(new RuntimeException("DB Fail"))
                .when(repository).save(incomingEvent);

        assertThrows(RuntimeException.class, () -> {
            service.processEvent(incomingEvent);
        });

        verify(repository, times(1)).save(incomingEvent);
        verifyNoInteractions(rabbitTemplate);
    }
}