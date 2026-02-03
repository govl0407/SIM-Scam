package com.example.be.service.test;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class testChatMemory {

    private final Map<String, Map<String, Object>> memory = new HashMap<>();

    public Map<String, Object> getSession(String sessionId) {
        return memory.computeIfAbsent(sessionId, id -> {
            Map<String, Object> session = new HashMap<>();
            session.put("currentEvent", null);
            session.put("eventLogs", new HashMap<String, String>());
            session.put("chatLogs", new ArrayList<Map<String, String>>());
            session.put("prompt", null);
            return session;
        });
    }

    //
    public String getCurrentEvent(String sessionId) {
        return (String) getSession(sessionId).get("currentEvent");
    }
    public void setCurrentEvent(String sessionId, String event) {
        getSession(sessionId).put("currentEvent", event);
    }
    // 현재 이벤트의 사용자 응답 업데이트
    public void updateCurrentEventMessage(
            String sessionId,
            String event,
            String userAnswer
    ) {
        Map<String, String> eventLogs = getEventLogs(sessionId);

        if (eventLogs.containsKey(event)) {
            eventLogs.put(event, userAnswer);
        }
    }
    //이벤트 로그
    @SuppressWarnings("unchecked")
    public Map<String, String> getEventLogs(String sessionId) {
        return (Map<String, String>) getSession(sessionId).get("eventLogs");
    }
    public void addEventLog(String sessionId, String event, String result) {
        getEventLogs(sessionId).put(event, result);
    }

    //채팅
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getChatLogs(String sessionId) {
        return (List<Map<String, String>>) getSession(sessionId).get("chatLogs");
    }

    public void addChatLog(String sessionId, String role, String text) {
        Map<String, String> log = new HashMap<>();
        log.put("role", role);
        log.put("text", text);
        getChatLogs(sessionId).add(log);
    }



    public void clear(String sessionId) {
        memory.remove(sessionId);
    }
}