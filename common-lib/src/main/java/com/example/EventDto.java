package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventDto {
    private String uuid;
    private LocalDateTime eventTime;

    @JsonProperty("sql_saved_at")
    private LocalDateTime sqlSavedAt; // null before SQL save
}
