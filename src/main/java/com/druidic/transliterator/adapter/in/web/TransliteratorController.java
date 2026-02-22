package com.druidic.transliterator.adapter.in.web;

import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import com.druidic.transliterator.port.in.TransliteratePort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Inbound web adapter — drives the application via HTTP form submissions.
 * Depends only on TransliteratePort; knows nothing about FutharkTransliterator.
 */
@Controller
public class TransliteratorController {

    private final TransliteratePort transliteratePort;

    public TransliteratorController(TransliteratePort transliteratePort) {
        this.transliteratePort = transliteratePort;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("inputText", "");
        model.addAttribute("result", null);
        return "index";
    }

    @PostMapping("/transliterate")
    public String transliterate(
            @RequestParam(defaultValue = "") String inputText,
            Model model) {

        TransliterationResult result = transliteratePort.transliterate(
                new TransliterationRequest(inputText)
        );

        model.addAttribute("inputText", inputText);
        model.addAttribute("result", result);
        return "index";
    }
}
