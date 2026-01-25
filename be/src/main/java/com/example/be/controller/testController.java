package com.example.be.controller;

import com.example.be.dto.userMessageDto;
import com.example.be.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/test")
public class testController {

    private final ChatService chatService;

    public testController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody userMessageDto request) {

        String sessionId = "test-session"; // 나중에 userId
        return chatService.chat(sessionId, request);
    }

    @GetMapping("/get")
    public String testGet() {
        return "hello";
    }
}


