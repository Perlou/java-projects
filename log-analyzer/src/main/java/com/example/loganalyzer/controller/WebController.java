package com.example.loganalyzer.controller;

import com.example.loganalyzer.service.AnalyzerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web 页面控制器
 */
@Controller
public class WebController {

    private final AnalyzerService analyzerService;

    public WebController(AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    /**
     * 主页
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("logCount", analyzerService.getLogCount());
        return "index";
    }
}
