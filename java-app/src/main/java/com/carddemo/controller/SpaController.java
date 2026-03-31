package com.carddemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {"/app", "/app/**"})
    public String forwardToReact() {
        return "forward:/app/index.html";
    }
}
