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
    @GetMapping("/persona")
    public Map<String, String> getPersona(
            @RequestParam("scenario") String scenario) {

        // 서비스에서 파싱된 페르소나 정보를 가져옵니다.
        return chatService.getPersonaInfo(scenario);
    }
    @GetMapping("/event-log")
    public Map<String, Object> getChatStatus(
            @RequestParam("scenario") String scenario, // 🎯 어떤 시나리오의 상태를 볼지 파라미터로 받음
            HttpSession session) {

        String sessionId = session.getId();
        String compositeKey = sessionId + ":" + scenario;

        Map<String, Object> response = new HashMap<>();

        // 🎯 요청하신 evnetlog(이벤트 기록)와 currentEvent(현재 진행 중인 이벤트) 구성
        response.put("eventLogs", ChatMemory.getEventLogs(compositeKey));
        response.put("currentEvent", ChatMemory.getCurrentEvent(compositeKey));

        return response;
    }

    @GetMapping("/reset")
    public Map<String, String> resetChat(
            @RequestParam(value = "scenario", required = false) String scenario, // 🎯 선택적 파라미터
            HttpSession session) {

        String sessionId = session.getId();
        Map<String, String> response = new HashMap<>();

        if (scenario != null && !scenario.isEmpty()) {
            // 1. 특정 시나리오만 초기화 (예: ?scenario=romance)
            String compositeKey = sessionId + ":" + scenario;
            ChatMemory.clear(compositeKey);

            response.put("status", "success");
            response.put("message", scenario + " 시나리오의 채팅 기록이 초기화되었습니다.");
        } else {
            // 2. 시나리오 파라미터가 없으면 세션 전체 초기화
            // ChatMemory 내부에서 sessionId로 시작하는 모든 키를 지우는 로직이 필요할 수 있습니다.
            // 여기서는 단순히 세션을 무효화하여 다음 접속 시 새 sessionId를 받게 합니다.
            ChatMemory.clear(sessionId); // 기본 sessionId 키 삭제
            session.invalidate();

            response.put("status", "success");
            response.put("message", "모든 채팅 기록과 세션이 초기화되었습니다.");
        }

        return response;
    }
}