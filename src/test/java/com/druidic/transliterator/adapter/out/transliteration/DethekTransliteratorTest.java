package com.druidic.transliterator.adapter.out.transliteration;

import com.druidic.transliterator.core.Script;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DethekTransliteratorTest {

    private final DethekTransliterator transliterator = new DethekTransliterator();

    private TransliterationResult transliterate(String input) {
        return transliterator.transliterate(new TransliterationRequest(input, Script.DETHEK));
    }

    @ParameterizedTest
    @CsvSource({
        "a, a", "b, b", "c, c", "d, d", "e, e", "f, f",
        "g, g", "h, h", "i, i", "j, j", "k, k", "l, l",
        "m, m", "n, n", "o, o", "p, p", "q, q", "r, r",
        "s, s", "t, t", "u, u", "v, v", "w, w", "x, x",
        "y, y", "z, z"
    })
    void transliteratesSingleLetters(String input, String expected) {
        assertEquals(expected, transliterate(input).runeText());
    }

    @Test
    void preservesSpaces() {
        assertEquals("hello world", transliterate("hello world").runeText());
    }

    @Test
    void preservesNewlines() {
        assertEquals("a\nb", transliterate("a\nb").runeText());
    }

    @Test
    void handlesUpperCase() {
        assertEquals("abc", transliterate("ABC").runeText());
    }

    @Test
    void dropsUnmappedCharacters() {
        assertEquals("ab", transliterate("a!b").runeText());
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
