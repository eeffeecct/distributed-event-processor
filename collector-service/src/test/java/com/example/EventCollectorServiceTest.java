package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventCollectorServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private EventCollectorService service;

    @Test
    void collectEvents_shouldReturnDataFromApi() {
        EventDto event1 = new EventDto();
        event1.setUuid("uuid-1");
        EventDto[] mockResponse = {event1};
    
        when(restTemplate.getForObject(anyString(), eq(EventDto[].class)))
                .thenReturn(mockResponse);

        service.collectEvents();

        verify(rabbitTemplate, times(1))
                .convertAndSend("events.raw", event1);
    }

    @Test
    void collectEvents_shouldDoNothing_whenApiReturnsEmpty() {
        when(restTemplate.getForObject(anyString(), eq(EventDto[].class)))
                .thenReturn(null);

        service.collectEvents();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(EventDto.class));
    }
}
