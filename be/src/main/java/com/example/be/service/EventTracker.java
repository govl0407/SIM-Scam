package com.example.be.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EventTracker {

    // sessionId -> 대기 중인 이벤트
    private final Map<String, String> pendingEvent = new HashMap<>();

    // sessionId -> 누적 로그
    private final Map<String, List<String>> logs = new HashMap<>();

    // GPT가 이벤트 발생시
    public void registerEvent(String sessionId, String event) {
        if (event != null) {
            pendingEvent.put(sessionId, event);
        }
    }

    // 사용자가 다음 턴에서 응답
    public void registerUserResponse(String sessionId, String userMessage) {
        if (!pendingEvent.containsKey(sessionId)) return;

        String event = pendingEvent.get(sessionId);
        String log = event + ": " + userMessage;

        logs.computeIfAbsent(sessionId, k -> new ArrayList<>())
                .add(log);

        pendingEvent.remove(sessionId);
    }

    public List<String> getLogs(String sessionId) {
        return logs.getOrDefault(sessionId, List.of());
    }
}