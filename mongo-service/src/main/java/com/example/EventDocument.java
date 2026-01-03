package com.example;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "archived_events") // имя таблицы в mongo
public class EventDocument {
    @Id
    private String uuid;
    private LocalDateTime eventTime;
    private LocalDateTime sqlSavedAt;
    private LocalDateTime mongoSavedAt;
}
