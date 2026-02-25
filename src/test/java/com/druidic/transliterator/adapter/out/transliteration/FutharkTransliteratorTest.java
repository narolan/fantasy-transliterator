package com.druidic.transliterator.adapter.out.transliteration;

import com.druidic.transliterator.core.Script;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class FutharkTransliteratorTest {

    private final FutharkTransliterator transliterator = new FutharkTransliterator();

    private TransliterationResult transliterate(String input) {
        return transliterator.transliterate(new TransliterationRequest(input, Script.ELDER_FUTHARK));
    }

    @ParameterizedTest
    @CsvSource({
        "a, ᚨ", "b, ᛒ", "c, ᚲ", "d, ᛞ", "e, ᛖ", "f, ᚠ",
        "g, ᚷ", "h, ᚺ", "i, ᛁ", "j, ᛃ", "k, ᚲ", "l, ᛚ",
        "m, ᛗ", "n, ᚾ", "o, ᛟ", "p, ᛈ", "q, ᚲ", "r, ᚱ",
        "s, ᛊ", "t, ᛏ", "u, ᚢ", "v, ᚢ", "w, ᚹ", "x, ᛊ",
        "y, ᛃ", "z, ᛉ"
    })
    void transliteratesSingleLetters(String input, String expected) {
        assertEquals(expected, transliterate(input).runeText());
    }

    @Test
    void preservesSpaces() {
        assertEquals("ᚺᛖᛚᛚᛟ ᚹᛟᚱᛚᛞ", transliterate("hello world").runeText());
    }

    @Test
    void preservesNewlines() {
        assertEquals("ᚨ\nᛒ", transliterate("a\nb").runeText());
    }

    @Test
    void handlesUpperCase() {
        assertEquals("ᚨᛒᚲ", transliterate("ABC").runeText());
    }

    @Test
    void dropsUnmappedCharacters() {
        assertEquals("ᚨᛒ", transliterate("a!b").runeText());
    }

    @Test
    void blankInputReturnsEmpty() {
        TransliterationResult result = transliterate("   ");
        assertEquals("", result.runeText());
    }

    @Test
    void emptyInputReturnsEmpty() {
        TransliterationResult result = transliterate("");
        assertEquals("", result.runeText());
    }
}
