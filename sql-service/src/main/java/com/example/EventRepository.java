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

    public int save(EventEntity event) {
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

    public Optional<EventEntity> findByUuid(String uuid) {
        String sql = "SELECT * FROM events WHERE uuid = ?";
        try {
            EventEntity event = jdbcTemplate.queryForObject(sql, eventRowMapper(), uuid);
            return Optional.ofNullable(event);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<EventEntity> eventRowMapper() {
        return (rs, rowNum) -> EventEntity.builder()
                .uuid(rs.getString("uuid"))
                .eventTime(rs.getTimestamp("event_time").toLocalDateTime())
                .build();
    }
}
