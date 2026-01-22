package com.example.be.controller;

import com.example.be.dto.testDto;
import com.example.be.service.gptService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // 프론트 테스트용 (나중에 제한)
public class testController {
<<<<<<< HEAD

    @PostMapping("/test/chat")
    public Map<String, String> receiveChat(@RequestBody testDto request) {
        System.out.println("📩 받은 메시지: " + request.getMessage());

        return Map.of("reply", " " + request.getMessage());
=======
    private final gptService gptService;

    @PostMapping("/test/chat")
    public String receiveChat(@RequestBody testDto request) {
        System.out.println("📩 받은 메시지: " + request.getMessage());
        return request.getMessage()+"에 대한 답장";
>>>>>>> 6748472a5daf91b9eb27c6a6638bb40b114d3ec6
    }

    @GetMapping("/test/get")
    public String testGet(){
        return "hello";
    }

    public testController(gptService gptService) {
        this.gptService = gptService;
    }

    @GetMapping("/test/gpt")
    public String testGpt() {
        String fixedMessage = "너는 지금 뭐하고 있어?";
        return gptService.textGpt(fixedMessage);
    }

}

