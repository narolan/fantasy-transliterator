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

    private static final int MAX_INPUT_LENGTH = 500;

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

        return handleTransliteration(text, script, model);
    }

    @PostMapping("/")
    public String transliterate(
            @RequestParam(defaultValue = "") String inputText,
            @RequestParam(defaultValue = "ELDER_FUTHARK") String script,
            Model model) {

        return handleTransliteration(inputText, script, model);
    }

    private String handleTransliteration(String inputText, String scriptParam, Model model) {
        Script selectedScript = parseScript(scriptParam);
        String trimmedInput = truncate(inputText);

        model.addAttribute("scripts", Script.values());
        model.addAttribute("selectedScript", selectedScript);

        if (!trimmedInput.isBlank()) {
            TransliterationResult result = transliterators.get(selectedScript)
                    .transliterate(new TransliterationRequest(trimmedInput, selectedScript));
            model.addAttribute("inputText", trimmedInput);
            model.addAttribute("result", result);
        } else {
            model.addAttribute("inputText", "");
            model.addAttribute("result", null);
        }

        return "index";
    }

    private Script parseScript(String raw) {
        try {
            return Script.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return Script.ELDER_FUTHARK;
        }
    }

    private String truncate(String input) {
        if (input.length() <= MAX_INPUT_LENGTH) {
            return input;
        }
        return input.substring(0, MAX_INPUT_LENGTH);
    }
}
