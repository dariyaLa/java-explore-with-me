package ru.practicum.requests;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Request {

    private long id;
    private long eventId;
    private long requesterId;
    private Instant created;
    private RequestState status;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("event_id", eventId);
        map.put("requester_id", requesterId);
        map.put("status", status.toString());
        return map;
    }
}
