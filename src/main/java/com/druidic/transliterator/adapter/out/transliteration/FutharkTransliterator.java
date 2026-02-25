package com.druidic.transliterator.adapter.out.transliteration;

import com.druidic.transliterator.core.LegendEntry;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import com.druidic.transliterator.port.in.TransliteratePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Outbound adapter — Elder Futhark transliteration engine.
 * Implements the input port. Contains all transliteration logic;
 * this is intentionally kept out of the core so the mapping strategy
 * (Elder Futhark, Younger Futhark, Anglo-Saxon futhorc…) is swappable
 * without touching ports or domain models.
 */
@Qualifier("elderFuthark")
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

        String runes = input.toLowerCase()
                .chars()
                .mapToObj(ch -> switch ((char) ch) {
                    case ' '  -> " ";
                    case '\n' -> "\n";
                    default   -> RUNE_MAP.getOrDefault((char) ch, "");
                })
                .collect(Collectors.joining());

        return new TransliterationResult(input, runes);
    }

    @Override
    public List<LegendEntry> getLegend() {
        return List.of(
            new LegendEntry("\u16A0", "F"),     new LegendEntry("\u16A2", "U/V"),
            new LegendEntry("\u16A6", "TH"),    new LegendEntry("\u16A8", "A"),
            new LegendEntry("\u16B1", "R"),     new LegendEntry("\u16B2", "K/C/Q"),
            new LegendEntry("\u16B7", "G"),     new LegendEntry("\u16B9", "W"),
            new LegendEntry("\u16BA", "H"),     new LegendEntry("\u16BE", "N"),
            new LegendEntry("\u16C1", "I"),     new LegendEntry("\u16C3", "J/Y"),
            new LegendEntry("\u16C8", "P"),     new LegendEntry("\u16C9", "Z"),
            new LegendEntry("\u16CA", "S/X"),   new LegendEntry("\u16CF", "T"),
            new LegendEntry("\u16D2", "B"),     new LegendEntry("\u16D6", "E"),
            new LegendEntry("\u16D7", "M"),     new LegendEntry("\u16DA", "L"),
            new LegendEntry("\u16DC", "NG"),    new LegendEntry("\u16DE", "D"),
            new LegendEntry("\u16DF", "O")
        );
    }
}
