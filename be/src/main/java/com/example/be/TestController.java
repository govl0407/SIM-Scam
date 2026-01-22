package com.example.be;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5174")
@RestController
public class TestController {

    @GetMapping("/api/hello")
    public String hello() {
        return "정민 바보";
    }
}
