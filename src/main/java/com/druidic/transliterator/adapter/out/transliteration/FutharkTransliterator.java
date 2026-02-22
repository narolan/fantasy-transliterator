package com.druidic.transliterator.adapter.out.transliteration;

import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import com.druidic.transliterator.port.in.TransliteratePort;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Outbound adapter — Elder Futhark transliteration engine.
 *
 * Implements the input port. Contains all transliteration logic;
 * this is intentionally kept out of the core so the mapping strategy
 * (Elder Futhark, Younger Futhark, Anglo-Saxon futhorc…) is swappable
 * without touching ports or domain models.
 */
@Component
public class FutharkTransliterator implements TransliteratePort {

    // Elder Futhark — one rune per Latin letter.
    // Letters with no direct equivalent are mapped to the closest phonetic rune.
    private static final Map<Character, String> RUNE_MAP = Map.ofEntries(
            Map.entry('a', "ᚨ"),  // Ansuz
            Map.entry('b', "ᛒ"),  // Berkano
            Map.entry('c', "ᚲ"),  // Kaunan  (hard-C)
            Map.entry('d', "ᛞ"),  // Dagaz
            Map.entry('e', "ᛖ"),  // Ehwaz
            Map.entry('f', "ᚠ"),  // Fehu
            Map.entry('g', "ᚷ"),  // Gebo
            Map.entry('h', "ᚺ"),  // Haglaz
            Map.entry('i', "ᛁ"),  // Isaz
            Map.entry('j', "ᛃ"),  // Jera
            Map.entry('k', "ᚲ"),  // Kaunan
            Map.entry('l', "ᛚ"),  // Laguz
            Map.entry('m', "ᛗ"),  // Mannaz
            Map.entry('n', "ᚾ"),  // Naudiz
            Map.entry('o', "ᛟ"),  // Othalan
            Map.entry('p', "ᛈ"),  // Pertho
            Map.entry('q', "ᚲ"),  // → Kaunan
            Map.entry('r', "ᚱ"),  // Raidho
            Map.entry('s', "ᛊ"),  // Sowilo
            Map.entry('t', "ᛏ"),  // Tiwaz
            Map.entry('u', "ᚢ"),  // Uruz
            Map.entry('v', "ᚢ"),  // → Uruz
            Map.entry('w', "ᚹ"),  // Wunjo
            Map.entry('x', "ᛊ"),  // → Sowilo (closest sibilant)
            Map.entry('y', "ᛃ"),  // → Jera
            Map.entry('z', "ᛉ")   // Algiz
    );

    @Override
    public TransliterationResult transliterate(TransliterationRequest request) {
        String input = request.rawText();

        if (input.isBlank()) {
            return new TransliterationResult(input, "");
        }

        StringBuilder runes = new StringBuilder();

        for (char ch : input.toLowerCase().toCharArray()) {
            if (ch == ' ') {
                runes.append(" ᛫ ");   // runic word separator
            } else if (ch == '\n') {
                runes.append('\n');
            } else {
                String rune = RUNE_MAP.get(ch);
                if (rune != null) {
                    runes.append(rune);
                }
                // punctuation and unmapped chars are intentionally dropped
            }
        }

        return new TransliterationResult(input, runes.toString());
    }
}
