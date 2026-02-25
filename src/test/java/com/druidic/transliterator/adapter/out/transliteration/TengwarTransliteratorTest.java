package com.druidic.transliterator.adapter.out.transliteration;

import com.druidic.transliterator.core.Script;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TengwarTransliteratorTest {

    private final TengwarTransliterator transliterator = new TengwarTransliterator();

    private TransliterationResult transliterate(String input) {
        return transliterator.transliterate(new TransliterationRequest(input, Script.TENGWAR));
    }

    @Test
    void vowelWithNoPrecedingConsonantUsesShortCarrier() {
        assertEquals("`#", transliterate("a").runeText());
    }

    @Test
    void vowelAfterConsonantAttachesToConsonant() {
        assertEquals("1#", transliterate("ta").runeText());
    }

    @Test
    void consecutiveVowelsEachGetCarrier() {
        assertEquals("`#`$", transliterate("ae").runeText());
    }

    @Test
    void consonantWithNoFollowingVowelFlushesBare() {
        assertEquals("1", transliterate("t").runeText());
    }

    @ParameterizedTest
    @CsvSource({
        "th, 3", "ch, a", "sh, u", "ph, e",
        "wh, Q", "ng, g", "ck, z", "qu, zz"
    })
    void handlesDigraphs(String input, String expected) {
        assertEquals(expected, transliterate(input).runeText());
    }

    @Test
    void digraphWithFollowingVowel() {
        assertEquals("3$", transliterate("the").runeText());
    }

    @Test
    void doubledConsonantUsesDoublingMark() {
        assertEquals("j~", transliterate("ll").runeText());
    }

    @Test
    void doubledConsonantWithVowel() {
        assertEquals("`#j~", transliterate("all").runeText());
    }

    @Test
    void doubledVowelDoesNotTriggerDoublingMark() {
        // "aa" — both chars match (ch == next) but 'a' is not in CONSONANTS,
        // so each vowel gets its own short carrier instead of a doubling mark
        assertEquals("`#`#", transliterate("aa").runeText());
    }

    @Test
    void preservesSpaces() {
        assertEquals("1# 1#", transliterate("ta ta").runeText());
    }

    @Test
    void preservesNewlines() {
        assertEquals("1#\n1#", transliterate("ta\nta").runeText());
    }

    @Test
    void dropsUnmappedCharacters() {
        assertEquals("1#", transliterate("t!a").runeText());
    }

    @Test
    void blankInputReturnsEmpty() {
        assertEquals("", transliterate("   ").runeText());
    }

    @Test
    void emptyInputReturnsEmpty() {
        assertEquals("", transliterate("").runeText());
    }

    @Test
    void handlesUpperCase() {
        assertEquals("1#", transliterate("TA").runeText());
    }

    @ParameterizedTest
    @CsvSource({
        "t, 1", "p, q", "d, 2", "b, w", "g, s",
        "f, e", "v, r", "n, 5", "m, t", "r, 6",
        "l, j", "s, 8", "z, i", "h, 9", "w, n",
        "y, h", "k, z", "c, a", "q, z"
    })
    void singleConsonantMappings(String input, String expected) {
        assertEquals(expected, transliterate(input).runeText());
    }

    @ParameterizedTest
    @CsvSource({
        "a, `#", "e, `$", "i, `%", "o, `^", "u, `&"
    })
    void standaloneVowelMappings(String input, String expected) {
        assertEquals(expected, transliterate(input).runeText());
    }
}
