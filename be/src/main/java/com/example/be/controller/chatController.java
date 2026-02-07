package com.example.be.controller;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.service.ChatMemory;
import com.example.be.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
            @RequestParam("scenario") String scenario,
            HttpSession session) {

        String sessionId = session.getId();
        String strJson = chatService.chat(sessionId, request, scenario);

        try {
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);
            Map<String, Object> filteredResponse = new HashMap<>();

            String compositeKey = sessionId + ":" + scenario;

            filteredResponse.put("sessionId", sessionId);       // 추가
            filteredResponse.put("scenario", scenario);
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
            @RequestParam("scenario") String scenario,
            HttpSession session) {

        String sessionId = session.getId();
        String strJson = chatService.eventResponse(sessionId, request, scenario);

        try {
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);
            Map<String, Object> filteredResponse = new HashMap<>();

            String compositeKey = sessionId + ":" + scenario;

            filteredResponse.put("sessionId", sessionId);       // 추가
            filteredResponse.put("scenario", scenario);
            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));
            filteredResponse.put("eventLogs", ChatMemory.getEventLogs(compositeKey));
            filteredResponse.put("currentEvent", ChatMemory.getCurrentEvent(compositeKey)); // ✅ 키 통일

            return filteredResponse;
        } catch (Exception e) {
            throw new RuntimeException("이벤트 응답 처리 실패", e);
        }
    }

    @GetMapping("/persona")
    public Map<String, String> getPersona(@RequestParam("scenario") String scenario) {
        return chatService.getPersonaInfo(scenario);
    }

    @GetMapping("/event-log")
    public Map<String, Object> getChatStatus(
            @RequestParam("scenario") String scenario,
            HttpSession session) {

        String sessionId = session.getId();
        String compositeKey = sessionId + ":" + scenario;

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("scenario", scenario);
        response.put("eventLogs", ChatMemory.getEventLogs(compositeKey));
        response.put("currentEvent", ChatMemory.getCurrentEvent(compositeKey));
        return response;
    }

    @GetMapping("/reset")
    public Map<String, String> resetChat(
            @RequestParam(value = "scenario", required = false) String scenario,
            HttpSession session) {

        String sessionId = session.getId();
        Map<String, String> response = new HashMap<>();

        if (scenario != null && !scenario.isEmpty()) {
            String compositeKey = sessionId + ":" + scenario;
            ChatMemory.clear(compositeKey);

            response.put("status", "success");
            response.put("message", scenario + " 시나리오의 채팅 기록이 초기화되었습니다.");
        } else {
            session.invalidate();

            response.put("status", "success");
            response.put("message", "모든 채팅 기록과 세션이 초기화되었습니다.");
        }
        return response;
    }
}
