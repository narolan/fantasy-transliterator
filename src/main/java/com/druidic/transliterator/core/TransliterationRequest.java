package com.druidic.transliterator.core;

/**
 * Value object representing the raw text submitted for transliteration.
 * Lives in core — no framework dependencies, no ports, no adapters.
 */
public record TransliterationRequest(String rawText, Script script) {

    public TransliterationRequest {
        rawText = rawText == null ? "" : rawText;
        script = script == null ? Script.ELDER_FUTHARK : script;
    }
}
