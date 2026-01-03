package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class EventRepository {

    private final JdbcTemplate jdbcTemplate;

    public int save(EventDto event) {
        String sql = """
                INSERT INTO events (uuid, event_time)
                VALUES (?, ?)
                ON CONFLICT (uuid) DO NOTHING
                """;

        return jdbcTemplate.update(
            sql,
            event.getUuid(),
            Timestamp.valueOf(event.getEventTime())
        );
    }

    public Optional<EventDto> findByUuid(String uuid) {
        String sql = "SELECT * FROM events WHERE uuid = ?";

        try {
            EventDto event = jdbcTemplate.queryForObject(sql, eventRowMapper(), uuid);
            return Optional.ofNullable(event);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // маппер
    private RowMapper<EventDto> eventRowMapper() {
        return (rs, rowNum) -> {
            EventDto dto = new EventDto();
            dto.setUuid(rs.getString("uuid"));

            Timestamp ts = rs.getTimestamp("event_time");
            if (ts != null) {
                dto.setEventTime(ts.toLocalDateTime());
            }
            return dto;
        };
    }

}
