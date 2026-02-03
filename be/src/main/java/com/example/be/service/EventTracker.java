package com.example.be.service;

import com.example.be.dto.EventLog;
import org.springframework.stereotype.Service;

import java.util.*;@Service
public class EventTracker {

    // sessionId -> 현재 진행 중 이벤트 (PENDING)
    private final Map<String, String> pendingEvent = new HashMap<>();

    // sessionId -> eventBase -> EventLog
    private final Map<String, Map<String, EventLog>> logs = new HashMap<>();

    /** GPT가 이벤트를 "제안" */
    public String tryRegisterEvent(String sessionId, String event) {
        if (event == null) return null;

        // 이미 대기 중 이벤트가 있으면 새 이벤트 차단
        if (pendingEvent.containsKey(sessionId)) {
            return pendingEvent.get(sessionId);
        }

        pendingEvent.put(sessionId, event);
        return event;
    }

    /** event-response에서만 확정 */
    public void resolveEvent(String sessionId, String event, String answer) {
        String base = extractBase(event);

        logs.computeIfAbsent(sessionId, k -> new HashMap<>())
                .putIfAbsent(base, new EventLog(event));

        logs.get(sessionId).get(base).resolve(answer);
        pendingEvent.remove(sessionId);
    }

    public String getPendingEvent(String sessionId) {
        return pendingEvent.get(sessionId);
    }

    public List<EventLog> getLogs(String sessionId) {
        return new ArrayList<>(
                logs.getOrDefault(sessionId, Map.of()).values()
        );
    }

    private String extractBase(String event) {
        int idx = event.lastIndexOf("_");
        return idx == -1 ? event : event.substring(0, idx);
    }
}