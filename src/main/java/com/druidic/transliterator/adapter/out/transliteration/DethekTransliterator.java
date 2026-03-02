package com.druidic.transliterator.adapter.out.transliteration;

import com.druidic.transliterator.core.LegendEntry;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;
import com.druidic.transliterator.port.in.TransliteratePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Qualifier("dethek")
@Component
public class DethekTransliterator implements TransliteratePort {

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
                    default   -> (ch >= 'a' && ch <= 'z') ? String.valueOf((char) ch) : "";
                })
                .collect(Collectors.joining());

        return new TransliterationResult(input, runes);
    }

    @Override
    public List<LegendEntry> getLegend() {
        return List.of(
            new LegendEntry("a", "A"),   new LegendEntry("b", "B"),
            new LegendEntry("c", "C/K"), new LegendEntry("d", "D"),
            new LegendEntry("e", "E"),   new LegendEntry("f", "F"),
            new LegendEntry("g", "G"),   new LegendEntry("h", "H"),
            new LegendEntry("i", "I"),   new LegendEntry("j", "J"),
            new LegendEntry("k", "K"),   new LegendEntry("l", "L"),
            new LegendEntry("m", "M"),   new LegendEntry("n", "N"),
            new LegendEntry("o", "O"),   new LegendEntry("p", "P"),
            new LegendEntry("q", "Q"),   new LegendEntry("r", "R"),
            new LegendEntry("s", "S"),   new LegendEntry("t", "T"),
            new LegendEntry("u", "U"),   new LegendEntry("v", "V"),
            new LegendEntry("w", "W/X/Z"), new LegendEntry("y", "Y")
        );
    }
}
