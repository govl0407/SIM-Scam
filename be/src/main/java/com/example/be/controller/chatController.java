package com.example.be.controller;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.service.test.testChatMemory;
import com.example.be.service.test.testChatService;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/chat")
public class chatController {

    private final testChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final com.example.be.service.test.testChatMemory testChatMemory;

    public chatController(testChatService chatService, testChatMemory testChatMemory) {
        this.chatService = chatService;
        this.testChatMemory = testChatMemory;
    }

    @PostMapping("/message")
    public Map<String, Object> chat(@RequestBody userMessageDto request) {

        String sessionId = "test-session";
        String strJson = chatService.chat(sessionId, request);
        try {
            // GPT가 준 JSON 문자열 → Map으로 변환
            return objectMapper.readValue(strJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 JSON 파싱 실패: " + strJson, e);
        }
    }
    @PostMapping("/event-response")
    public Map<String, Object> eventResponse(
            @RequestBody EventResponseDto request
    ) {
        String sessionId = "test-session";
        String strJson = chatService.eventResponse(sessionId, request);
        try {
            // GPT가 준 JSON 문자열 → Map으로 변환
            return objectMapper.readValue(strJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 JSON 파싱 실패: " + strJson, e);
        }

    }
    @GetMapping("/event-logs")
    public Map<String, Object> getEventLogs(
            @RequestParam String sessionId
    ) {
        Map<String, Object> response = new HashMap<>();

        response.put("sessionId", sessionId);
        response.put(
                "currentEvent",
                testChatMemory.getCurrentEvent(sessionId)
        );
        response.put(
                "eventLogs",
                testChatMemory.getEventLogs(sessionId)
        );

        return response;
    }
}
