package com.example.be.controller;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.service.ChatMemory;
import com.example.be.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/chat")
public class chatController {

    private final ChatService chatService;
    private final ChatMemory ChatMemory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public chatController(ChatService chatService, ChatMemory ChatMemory) {
        this.chatService = chatService;
        this.ChatMemory = ChatMemory;
    }

    @PostMapping("/message")
    public Map<String, Object> chat(
            @RequestBody userMessageDto request,
            @RequestParam("scenario") String scenario, // 🎯 시나리오 쿼리 파라미터 추가
            HttpSession session) {

        String sessionId = session.getId();
        // 서비스에서 sessionId와 scenario를 조합해 사용할 것이므로 둘 다 전달
        String strJson = chatService.chat(sessionId, request, scenario);

        try {
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);
            Map<String, Object> filteredResponse = new HashMap<>();

            // 🎯 compositeKey를 고려하여 ChatMemory 조회
            String compositeKey = sessionId + ":" + scenario;

            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("end", fullGptResponse.get("end"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));
            filteredResponse.put("currentEvent", ChatMemory.getCurrentEvent(compositeKey));

            return filteredResponse;
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }
    }

    @PostMapping("/event-response")
    public Map<String, Object> eventResponse(
            @RequestBody EventResponseDto request,
            @RequestParam("scenario") String scenario, // 🎯 시나리오 추가
            HttpSession session) {

        String sessionId = session.getId();
        String strJson = chatService.eventResponse(sessionId, request, scenario); // 🎯 시나리오 전달

        try {
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);
            Map<String, Object> filteredResponse = new HashMap<>();

            String compositeKey = sessionId + ":" + scenario; // 🎯 compositeKey 생성

            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));
            filteredResponse.put("eventLogs", ChatMemory.getEventLogs(compositeKey)); // 🎯 Key 수정
            filteredResponse.put("CurrentEvent", ChatMemory.getCurrentEvent(compositeKey)); // 🎯 Key 수정

            return filteredResponse;
        } catch (Exception e) {
            throw new RuntimeException("이벤트 응답 처리 실패", e);
        }
    }

    @GetMapping("/reset")
    public Map<String, String> resetChat(HttpSession session) {
        String sessionId = session.getId();

        // 1. 서버 메모리(Chat Memory)에서 해당 세션 데이터 삭제
        ChatMemory.clear(sessionId);

        // 2. 세션 무효화 (기존 세션 ID를 버리고 다음 요청 시 새로 생성)
        session.invalidate();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "채팅 기록과 세션이 초기화되었습니다.");

        return response;
    }
}