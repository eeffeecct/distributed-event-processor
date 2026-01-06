package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCollectorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventCollectorService service;

    private final String TEST_URL = "http://test-api.com/events";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiUrl", TEST_URL);
    }

    @Test
    void collectEvents_shouldSendEventsToRabbit_whenApiReturnsData() {
        EventDto event1 = EventDto.builder().uuid("uuid-1").build();
        EventDto event2 = EventDto.builder().uuid("uuid-2").build();

        EventDto[] mockResponse = {event1, event2};

        when(restTemplate.getForObject(TEST_URL, EventDto[].class))
                .thenReturn(mockResponse);

        service.collectEvents();

        verify(rabbitTemplate, times(1))
                .convertAndSend(RabbitQueueConstants.QUEUE_RAW_EVENTS, event1);

        verify(rabbitTemplate, times(1))
                .convertAndSend(RabbitQueueConstants.QUEUE_RAW_EVENTS, event2);
    }

    @Test
    void collectEvents_shouldDoNothing_whenApiReturnsNull() {
        when(restTemplate.getForObject(TEST_URL, EventDto[].class))
                .thenReturn(null);

        service.collectEvents();

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void collectEvents_shouldDoNothing_whenApiReturnsEmptyArray() {
        when(restTemplate.getForObject(TEST_URL, EventDto[].class))
                .thenReturn(new EventDto[0]);

        service.collectEvents();

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void collectEvents_shouldHandleException_whenApiFails() {
        when(restTemplate.getForObject(TEST_URL, EventDto[].class))
                .thenThrow(new RuntimeException("API unavailable"));

        service.collectEvents();

        verifyNoInteractions(rabbitTemplate);
    }
}