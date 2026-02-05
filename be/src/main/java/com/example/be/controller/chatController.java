package com.example.be.controller;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.service.test.testChatMemory;
import com.example.be.service.test.testChatService;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
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
            // 1. GPT 전체 응답 파싱
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);

            // 2. 클라이언트에 보낼 필터링된 응답 생성
            Map<String, Object> filteredResponse = new HashMap<>();

            // 🎯 요청하신 특정 필드만 추출
            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("end", fullGptResponse.get("end"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));

            // 🎯 서버 상태 값 (현재 이벤트 정보)
            filteredResponse.put("currentEvent", testChatMemory.getCurrentEvent(sessionId));


            return filteredResponse;

        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 JSON 파싱 실패: " + strJson, e);
        }
    }@PostMapping("/event-response")
    public Map<String, Object> eventResponse(
            @RequestBody EventResponseDto request
    ) {
        String sessionId = "test-session";
        String strJson = chatService.eventResponse(sessionId, request);

        try {
            // 1. GPT 응답 파싱
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);

            // 2. 클라이언트에 보낼 필터링된 응답 생성
            Map<String, Object> filteredResponse = new HashMap<>();

            // 🎯 요청하신 특정 필드 추출
            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));

            // 🎯 서버의 최신 이벤트 로그 및 상태 (CurrentEvent는 보통 null로 반환될 단계)
            filteredResponse.put("eventLogs", testChatMemory.getEventLogs(sessionId));
            filteredResponse.put("CurrentEvent", testChatMemory.getCurrentEvent(sessionId));

            return filteredResponse;

        } catch (Exception e) {
            throw new RuntimeException("이벤트 응답 처리 중 JSON 파싱 실패: " + strJson, e);
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
