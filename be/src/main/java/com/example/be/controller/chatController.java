package com.example.be.controller;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.service.test.testChatMemory;
import com.example.be.service.test.testChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/chat")
public class chatController {

    private final testChatService chatService;
    private final testChatMemory testChatMemory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public chatController(testChatService chatService, testChatMemory testChatMemory) {
        this.chatService = chatService;
        this.testChatMemory = testChatMemory;
    }

    @PostMapping("/message")
    public Map<String, Object> chat(@RequestBody userMessageDto request, HttpSession session) {
        // 1. 세션 ID 가져오기 (없으면 자동 생성됨)
        String sessionId = session.getId();

        String strJson = chatService.chat(sessionId, request);

        try {
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);
            Map<String, Object> filteredResponse = new HashMap<>();

            // 🎯 요청하신 필드 필터링
            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("end", fullGptResponse.get("end"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));
            filteredResponse.put("currentEvent", testChatMemory.getCurrentEvent(sessionId));

            return filteredResponse;

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }
    }

    @PostMapping("/event-response")
    public Map<String, Object> eventResponse(@RequestBody EventResponseDto request, HttpSession session) {
        // 2. 동일한 세션 ID 사용
        String sessionId = session.getId();

        String strJson = chatService.eventResponse(sessionId, request);

        try {
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);
            Map<String, Object> filteredResponse = new HashMap<>();

            // 🎯 요청하신 필드 필터링
            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));
            filteredResponse.put("eventLogs", testChatMemory.getEventLogs(sessionId));
            filteredResponse.put("CurrentEvent", testChatMemory.getCurrentEvent(sessionId));

            return filteredResponse;

        } catch (Exception e) {
            throw new RuntimeException("이벤트 응답 처리 실패", e);
        }
    }
}