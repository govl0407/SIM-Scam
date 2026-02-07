package com.example.be.controller;

import com.example.be.dto.EventResponseDto;
import com.example.be.dto.userMessageDto;
import com.example.be.service.ChatMemory;
import com.example.be.service.ChatService;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(
        origins = "http://simscam-frontend.s3-website.kr.object.ncloudstorage.com",
        allowCredentials = "true",
        allowedHeaders = "*"
)
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

    /**
     * 🎯 HttpSession 대신 프론트에서 보낸 'sid'를 직접 사용합니다.
     */
    @PostMapping("/message")
    public Map<String, Object> chat(
            @RequestBody userMessageDto request,
            @RequestParam("scenario") String scenario,
            @RequestParam("sid") String sid) { // 👈 세션 대신 파라미터로 받음

        // 프론트에서 받은 sid를 그대로 서비스에 전달
        String strJson = chatService.chat(sid, request, scenario);

        try {
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);
            Map<String, Object> filteredResponse = new HashMap<>();

            // 🎯 프론트에서 받은 sid와 scenario를 조합해 메모리 조회
            String compositeKey = sid + ":" + scenario;

            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("end", fullGptResponse.get("end"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));
            filteredResponse.put("currentEvent", ChatMemory.getCurrentEvent(compositeKey));

            // 프론트 확인용으로 sid(sessionId)를 그대로 반환
            filteredResponse.put("sessionId", sid);

            return filteredResponse;
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 파싱 실패", e);
        }
    }

    @PostMapping("/event-response")
    public Map<String, Object> eventResponse(
            @RequestBody EventResponseDto request,
            @RequestParam("scenario") String scenario,
            @RequestParam("sid") String sid) { // 👈 sid 적용

        String strJson = chatService.eventResponse(sid, request, scenario);

        try {
            Map<String, Object> fullGptResponse = objectMapper.readValue(strJson, Map.class);
            Map<String, Object> filteredResponse = new HashMap<>();

            String compositeKey = sid + ":" + scenario;

            filteredResponse.put("text", fullGptResponse.get("text"));
            filteredResponse.put("image", fullGptResponse.get("image"));
            filteredResponse.put("단계", fullGptResponse.get("단계"));
            filteredResponse.put("eventLogs", ChatMemory.getEventLogs(compositeKey));
            filteredResponse.put("currentEvent", ChatMemory.getCurrentEvent(compositeKey));
            filteredResponse.put("sessionId", sid);

            return filteredResponse;
        } catch (Exception e) {
            throw new RuntimeException("이벤트 응답 처리 실패", e);
        }
    }

    @GetMapping("/persona")
    public Map<String, String> getPersona(
            @RequestParam("scenario") String scenario,
            @RequestParam(value = "sid", required = false) String sid) { // 👈 선택적 파라미터로 sid 추가 가능
        return chatService.getPersonaInfo(scenario);
    }

    @GetMapping("/event-log")
    public Map<String, Object> getChatStatus(
            @RequestParam("scenario") String scenario,
            @RequestParam("sid") String sid) { // 👈 sid 적용

        String compositeKey = sid + ":" + scenario;
        Map<String, Object> response = new HashMap<>();

        response.put("eventLogs", ChatMemory.getEventLogs(compositeKey));
        response.put("currentEvent", ChatMemory.getCurrentEvent(compositeKey));

        return response;
    }

    @GetMapping("/reset")
    public Map<String, String> resetChat(
            @RequestParam(value = "scenario", required = false) String scenario,
            @RequestParam("sid") String sid) { // 👈 sid 적용

        Map<String, String> response = new HashMap<>();

        if (scenario != null && !scenario.isEmpty()) {
            String compositeKey = sid + ":" + scenario;
            ChatMemory.clear(compositeKey);
            response.put("message", scenario + " 시나리오 초기화 성공");
        } else {
            // 해당 sid로 시작하는 모든 기록 삭제 (ChatMemory에 구현된 방식에 따라 다름)
            ChatMemory.clear(sid);
            response.put("message", "사용자 전체 기록 초기화 성공");
        }

        response.put("status", "success");
        return response;
    }
}