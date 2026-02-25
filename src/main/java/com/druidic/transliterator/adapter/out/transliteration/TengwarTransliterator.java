package com.druidic.transliterator.adapter.out.transliteration;

import com.druidic.transliterator.core.LegendEntry;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import com.druidic.transliterator.port.in.TransliteratePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tengwar transliterator using English Tengwar Mode with Tengwar Annatar font encoding.
 * Uses Daniel Smith's de facto standard encoding — glyphs are mapped to Latin/special
 * characters based on the QWERTY keyboard layout, NOT to their phonetic Latin equivalents.
 * Vowels (tehtar) are diacritics placed on the preceding consonant. If a vowel has no
 * preceding consonant it is placed on the short carrier (backtick).
 */
@Qualifier("tengwar")
@Component
public class TengwarTransliterator implements TransliteratePort {

    // Short carrier — for vowels with no preceding consonant
    private static final String SHORT_CARRIER = "`";

    // Doubling mark
    private static final String DOUBLE_MARK = "~";

    // Vowel tehtar — diacritics placed after the preceding tengwa
    private static final Map<Character, String> VOWELS = Map.of(
            'a', "#",
            'e', "$",
            'i', "%",
            'o', "^",
            'u', "&"
    );

    // Digraphs — checked before single consonants
    private static final Map<String, String> DIGRAPHS = Map.of(
            "th", "3",   // súle
            "qu", "zz",  // quesse (q already maps to quesse so qu = quesse)
            "wh", "Q",   // hwesta
            "ch", "a",   // calma
            "sh", "u",   // harma
            "ph", "e",   // formen
            "ng", "g",   // nwalme
            "ck", "z"    // quesse
    );

    // Single consonants
    private static final Map<Character, String> CONSONANTS = Map.ofEntries(
            Map.entry('t', "1"),   // tinco
            Map.entry('p', "q"),   // parma
            Map.entry('c', "a"),   // calma
            Map.entry('k', "z"),   // quesse
            Map.entry('d', "2"),   // ando
            Map.entry('b', "w"),   // umbar
            Map.entry('g', "s"),   // ungwe
            Map.entry('f', "e"),   // formen
            Map.entry('v', "r"),   // ampa
            Map.entry('n', "5"),   // numen
            Map.entry('m', "t"),   // malta
            Map.entry('r', "6"),   // ore
            Map.entry('l', "j"),   // lambe
            Map.entry('s', "8"),   // silme
            Map.entry('z', "i"),   // esse
            Map.entry('h', "9"),   // hyarmen
            Map.entry('w', "n"),   // vala
            Map.entry('y', "h"),   // anna
            Map.entry('q', "z"),   // → quesse
            Map.entry('x', "8z")   // s+k cluster (silme + quesse)
    );

    @Override
    public TransliterationResult transliterate(TransliterationRequest request) {
        String input = request.rawText();
        if (input.isBlank()) {
            return new TransliterationResult(input, "");
        }

        String lower = input.toLowerCase();
        StringBuilder out = new StringBuilder();
        StringBuilder pending = new StringBuilder();

        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            String lookahead = i + 1 < lower.length() ? "" + ch + lower.charAt(i + 1) : "";

            if (ch == ' ' || ch == '\n') {
                flushPending(out, pending);
                out.append(ch == ' ' ? " " : "\n");
            } else if (DIGRAPHS.containsKey(lookahead)) {
                flushPending(out, pending);
                pending.append(DIGRAPHS.get(lookahead));
                i++;
            } else if (!lookahead.isEmpty() && ch == lower.charAt(i + 1) && CONSONANTS.containsKey(ch)) {
                flushPending(out, pending);
                pending.append(CONSONANTS.get(ch)).append(DOUBLE_MARK);
                i++;
            } else if (VOWELS.containsKey(ch)) {
                out.append(pending.isEmpty() ? SHORT_CARRIER : pending.toString()).append(VOWELS.get(ch));
                pending.setLength(0);
            } else if (CONSONANTS.containsKey(ch)) {
                flushPending(out, pending);
                pending.append(CONSONANTS.get(ch));
            }
            // unmapped — drop
        }

        flushPending(out, pending);
        return new TransliterationResult(input, out.toString());
    }

    private void flushPending(StringBuilder out, StringBuilder pending) {
        if (!pending.isEmpty()) {
            out.append(pending);
            pending.setLength(0);
        }
    }

    @Override
    public List<LegendEntry> getLegend() {
        return List.of(
            new LegendEntry("1", "T"),    new LegendEntry("q", "P"),
            new LegendEntry("a", "CH"),   new LegendEntry("z", "K/C/Q"),
            new LegendEntry("2", "D"),    new LegendEntry("w", "B"),
            new LegendEntry("s", "G"),    new LegendEntry("3", "TH"),
            new LegendEntry("e", "F/PH"), new LegendEntry("u", "SH"),
            new LegendEntry("r", "V"),    new LegendEntry("5", "N"),
            new LegendEntry("t", "M"),    new LegendEntry("g", "NG"),
            new LegendEntry("6", "R"),    new LegendEntry("j", "L"),
            new LegendEntry("8", "S"),    new LegendEntry("i", "Z"),
            new LegendEntry("9", "H"),    new LegendEntry("n", "W"),
            new LegendEntry("h", "Y"),    new LegendEntry("Q", "WH")
        );
    }
}