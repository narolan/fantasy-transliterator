package com.druidic.transliterator.adapter.in.web;

import com.druidic.transliterator.core.Script;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import com.druidic.transliterator.port.in.TransliteratePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Inbound web adapter — drives the application via HTTP form submissions.
 * Depends only on TransliteratePort; knows nothing about FutharkTransliterator.
 */
@Controller
public class TransliteratorController {


    private final Map<Script, TransliteratePort> transliterators;

    public TransliteratorController(@Qualifier("elderFuthark") TransliteratePort elderFutharkTransliterator,
                                    @Qualifier("tengwar") TransliteratePort tengwarTransliterator) {
        this.transliterators = Map.of(
                Script.ELDER_FUTHARK, elderFutharkTransliterator,
                Script.TENGWAR, tengwarTransliterator
        );
    }

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "")              String text,
            @RequestParam(defaultValue = "ELDER_FUTHARK") String script,
            Model model) {

        Script selectedScript;
        try {
            selectedScript = Script.valueOf(script);
        } catch (IllegalArgumentException e) {
            selectedScript = Script.ELDER_FUTHARK;
        }
        model.addAttribute("scripts", Script.values());
        model.addAttribute("selectedScript", selectedScript);

        if (!text.isBlank()) {
            TransliterationResult result = transliterators.get(selectedScript)
                    .transliterate(new TransliterationRequest(text, selectedScript));
            model.addAttribute("inputText", text);
            model.addAttribute("result", result);
        } else {
            model.addAttribute("inputText", "");
            model.addAttribute("result", null);
        }

        return "index";
    }

    @PostMapping("/")
    public String transliterate(
            @RequestParam(defaultValue = "") String inputText,
            @RequestParam(defaultValue = "ELDER_FUTHARK") String script,
            Model model) {

        Script selectedScript;
        try {
            selectedScript = Script.valueOf(script);
        } catch (IllegalArgumentException e) {
            selectedScript = Script.ELDER_FUTHARK;
        }
        TransliteratePort transliterator = transliterators.get(selectedScript);

        TransliterationResult result = transliterator.transliterate(
                new TransliterationRequest(inputText, selectedScript)
        );

        model.addAttribute("inputText", inputText);
        model.addAttribute("result", result);
        model.addAttribute("scripts", Script.values());
        model.addAttribute("selectedScript", selectedScript);
        return "index";
    }
}
