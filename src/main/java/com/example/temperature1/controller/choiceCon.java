package com.example.temperature1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class choiceCon {

    @GetMapping("/choice")
    public String showChoicePage() {
        return "choice";
    }

    // 다른 매핑 메소드들...
}