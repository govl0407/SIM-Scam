package com.example.be.controller;

import com.example.be.dto.testDto;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*") // 프론트 테스트용 (나중에 제한)
public class testController {
    @PostMapping("/test/chat")
    public void receiveChat(@RequestBody testDto request) {
        System.out.println("📩 받은 메시지: " + request.getMessage());
    }
    @GetMapping("/test/get")
    public String testGet(){
        return "hello";
    }
}
