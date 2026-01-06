package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        EventDto incomingEvent = EventDto.builder()
                .uuid("test-uuid-new")
                .eventTime(LocalDateTime.now())
                .build();

        when(repository.save(any(EventEntity.class))).thenReturn(1);

        service.processEvent(incomingEvent);

        ArgumentCaptor<EventEntity> entityCaptor = ArgumentCaptor.forClass(EventEntity.class);
        verify(repository, times(1)).save(entityCaptor.capture());

        EventEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getUuid()).isEqualTo(incomingEvent.getUuid());
        assertThat(capturedEntity.getEventTime()).isEqualTo(incomingEvent.getEventTime());

        verify(rabbitTemplate, times(1))
            .convertAndSend(
                eq(RabbitQueueConstants.QUEUE_PROCESSED_EVENTS),
                any(MongoEventMessage.class)
            );
    }

    @Test
    void processEvent_shouldIgnore_whenDuplicate() {
        EventDto incomingEvent = EventDto.builder()
                .uuid("test-uuid-duplicate")
                .eventTime(LocalDateTime.now())
                .build();

        when(repository.save(any(EventEntity.class))).thenReturn(0);

        service.processEvent(incomingEvent);

        verify(repository, times(1)).save(any(EventEntity.class));
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void processEvent_shouldThrowException_whenDbFails() {
        EventDto incomingEvent = EventDto.builder()
                .uuid("test-uuid-sql-error")
                .build();

        doThrow(new RuntimeException("DB Fail"))
                .when(repository).save(any(EventEntity.class));

        assertThrows(RuntimeException.class, () -> {
            service.processEvent(incomingEvent);
        });

        verify(repository, times(1)).save(any(EventEntity.class));
        verifyNoInteractions(rabbitTemplate);
    }
}