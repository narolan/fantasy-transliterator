package com.druidic.transliterator.adapter.in.web;

import com.druidic.transliterator.core.Script;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import com.druidic.transliterator.port.in.TransliteratePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransliterationApiController {

    private static final int MAX_INPUT_LENGTH = 500;

    private final Map<Script, TransliteratePort> transliterators;

    public TransliterationApiController(@Qualifier("elderFuthark") TransliteratePort elderFutharkTransliterator,
                                        @Qualifier("tengwar") TransliteratePort tengwarTransliterator,
                                        @Qualifier("dethek") TransliteratePort dethekTransliterator) {
        this.transliterators = Map.of(
                Script.ELDER_FUTHARK, elderFutharkTransliterator,
                Script.TENGWAR, tengwarTransliterator,
                Script.DETHEK, dethekTransliterator
        );
    }

    @GetMapping("/transliterate")
    public Map<String, String> transliterate(
            @RequestParam String text,
            @RequestParam(defaultValue = "ELDER_FUTHARK") String script) {

        Script selectedScript = parseScript(script);
        String trimmed = text.length() > MAX_INPUT_LENGTH ? text.substring(0, MAX_INPUT_LENGTH) : text;

        if (trimmed.isBlank()) {
            return Map.of("runeText", "");
        }

        TransliteratePort transliterator = transliterators.get(selectedScript);
        if (transliterator == null) {
            transliterator = transliterators.get(Script.ELDER_FUTHARK);
        }

        TransliterationResult result = transliterator
                .transliterate(new TransliterationRequest(trimmed, selectedScript));

        return Map.of("runeText", result.runeText());
    }

    private Script parseScript(String raw) {
        try {
            return Script.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return Script.ELDER_FUTHARK;
        }
    }
}
