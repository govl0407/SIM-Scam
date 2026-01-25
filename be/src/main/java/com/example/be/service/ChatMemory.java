package com.example.be.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ChatMemory {

    // sessionId → 대화 목록
    private final Map<String, List<Map<String, String>>> memory = new HashMap<>();//대화를 저장하는 해시맵

    public List<Map<String, String>> getMessages(String sessionId) {
        return memory.computeIfAbsent(sessionId, k -> new ArrayList<>());//해당 세션 아이디의 대화가 없으면 빈 배열 반환
    }

    public void clear(String sessionId) {
        memory.remove(sessionId);
    }
}
