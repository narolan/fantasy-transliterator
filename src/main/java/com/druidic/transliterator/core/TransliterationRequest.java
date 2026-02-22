package com.druidic.transliterator.core;

/**
 * Value object representing the raw text submitted for transliteration.
 * Lives in core — no framework dependencies, no ports, no adapters.
 */
public record TransliterationRequest(String rawText) {

    public TransliterationRequest {
        rawText = rawText == null ? "" : rawText;
    }
}
