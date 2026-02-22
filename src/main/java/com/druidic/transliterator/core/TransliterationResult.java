package com.druidic.transliterator.core;

/**
 * Value object representing the outcome of a transliteration.
 * Carries both the original input and the resulting rune string.
 * Lives in core — no framework dependencies, no ports, no adapters.
 */
public record TransliterationResult(String originalText, String runeText) {

    public boolean hasContent() {
        return runeText != null && !runeText.isBlank();
    }
}
