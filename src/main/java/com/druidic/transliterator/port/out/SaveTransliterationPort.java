package com.druidic.transliterator.port.out;

import com.druidic.transliterator.core.TransliterationResult;

/**
 * Output port — defines what the application needs from the outside world.
 * Wire an adapter (e.g. adapter/out/persistence/) when you need to persist results.
 */
public interface SaveTransliterationPort {

    void save(TransliterationResult result);
}
