package com.example.canteen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoPageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/demo-role-board.html";
    }
}
