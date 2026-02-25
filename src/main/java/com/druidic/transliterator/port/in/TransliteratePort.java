package com.druidic.transliterator.port.in;

import com.druidic.transliterator.core.LegendEntry;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;

import java.util.List;

/**
 * Input port — the contract the outside world uses to drive the application.
 * The web adapter depends on this interface only; it never touches any implementation.
 */
public interface TransliteratePort {
    TransliterationResult transliterate(TransliterationRequest request);
    List<LegendEntry> getLegend();
}
