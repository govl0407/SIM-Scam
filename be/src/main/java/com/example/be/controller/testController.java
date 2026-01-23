package com.example.be.controller;

import com.example.be.dto.testDto;
import com.example.be.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class testController {

    private final ChatService chatService;

    public testController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/test/chat")
    public String chat(@RequestBody testDto request) {

        String sessionId = "test-session"; // 나중에 userId
        return chatService.chat(sessionId, request.getMessage());
    }

    @GetMapping("/test/get")
    public String testGet() {
        return "hello";
    }
}


