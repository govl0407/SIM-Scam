package com.example.be.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ChatMemory {

    // sessionId → 대화 목록
    private final Map<String, List<Map<String, String>>> memory = new HashMap<>();

    public List<Map<String, String>> getMessages(String sessionId) {
        return memory.computeIfAbsent(sessionId, k -> new ArrayList<>());
    }

    public void clear(String sessionId) {
        memory.remove(sessionId);
    }
}
