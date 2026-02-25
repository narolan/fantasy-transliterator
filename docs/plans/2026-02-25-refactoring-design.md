# Refactoring Design — Frontend Modularization + Data-Driven Legends

## Goals

- Reduce file complexity by splitting large files into focused units
- Make adding new scripts require only one Java class (no HTML edits)
- Add tests for existing transliterators and controller as a safety net

## 1. CSS Modularization

Split `main.css` (474 lines) into focused files:

```
static/css/
├── main.css         — @import entry point only
├── variables.css    — :root vars, @font-face, reset (~30 lines)
├── animations.css   — floatRune, runeGlow, glowPulse, fadeSlideIn, rune-bg spans (~50 lines)
├── layout.css       — page-wrapper, site-header, main-card, footer (~70 lines)
├── components.css   — input, script-selector, buttons, output, copy-row, legend (~200 lines)
└── responsive.css   — @media queries for mobile + reduced-motion (~15 lines)
```

No CSS changes — purely structural file splitting.

## 2. Data-Driven Legends

### New core class

- `LegendEntry` record: `record LegendEntry(String glyph, String label)`

### Interface change

- `TransliteratePort` gains `List<LegendEntry> getLegend()`

### Transliterator changes

- `FutharkTransliterator.getLegend()` returns 23 entries (A–Z mapped runes)
- `TengwarTransliterator.getLegend()` returns 22 entries (consonants + digraphs)

### Controller change

- Passes `legend` list to the model from the selected transliterator

### Template change

- Replace two hardcoded `<details>` legend blocks (~55 lines) with one generic block (~10 lines) that iterates over `${legend}`
- Move footer description text into `Script` enum as a `description` field, replacing per-script `th:if` blocks

## 3. Tests

### Unit tests (no Spring context)

- `FutharkTransliteratorTest` — all 26 letter mappings, spaces, blank input, unmapped chars
- `TengwarTransliteratorTest` — vowels on carriers, vowels on consonants, digraphs, doubled consonants, blank input, unmapped chars

### Integration test

- `TransliteratorControllerTest` (@WebMvcTest) — GET with params, POST, script fallback, truncation
