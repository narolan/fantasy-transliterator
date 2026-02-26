# App Improvements Design — v2.0

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add six incremental features to the Fantasy Transliterator — script-themed UI, Dethek (D&D Dwarvish) script, live preview, session history, image export, and a feedback form. Each feature is an independent, shippable PR.

**Architecture:** Server-side rendering (Thymeleaf) stays as the core. Live preview adds a thin AJAX layer on top. No SPA rewrite. Hexagonal architecture unchanged — new scripts are new adapters.

**Tech Stack:** Java 17, Spring Boot 3.2.0, Thymeleaf, Vanilla JS, CSS custom properties, Gradle 8.5

---

## Feature 1: Script-Themed UI

### What
The entire page changes visual mood based on the selected script. Colors, glow effects, background glyphs, and accent tones shift to match the world of each script.

### Theme Definitions

**Elder Futhark (current — becomes the default/fallback):**
- Background: deep purple-black (`#0c0b0f`)
- Accents: gold/amber (`#c9a84c`, `#e8c97a`)
- Glow: warm gold (`rgba(201,168,76,...)`)
- Mood: ancient Nordic stone, firelit hall
- Background glyphs: existing Elder Futhark runes (ᚠ ᚢ ᚦ ᚨ ᚱ ᚲ ...)

**Tengwar (Elvish):**
- Background: deep midnight blue (`#0a0d14`)
- Surface: dark blue-grey (`#121825`)
- Accents: silver-white (`#c0cde0`, `#e0e8f4`)
- Glow: cool silver-blue (`rgba(192,205,224,...)`)
- Mood: starlit Rivendell, ethereal moonlight
- Background glyphs: Tengwar characters rendered in Tengwar Annatar font

**Dethek (Dwarvish):**
- Background: deep charcoal-brown (`#0f0c09`)
- Surface: dark warm grey (`#1c1814`)
- Accents: forge orange/copper (`#d4722a`, `#e8943c`)
- Glow: warm ember (`rgba(212,114,42,...)`)
- Mood: underground forge, hewn stone, molten metal
- Background glyphs: Dethek characters (from the Dethek font)

### Implementation

**Step 1: Define CSS variable sets per theme**

In `variables.css`, add theme classes that override `:root` variables:

```css
/* Default (Elder Futhark) uses :root values — no changes needed */

body.theme-tengwar {
    --color-bg:         #0a0d14;
    --color-surface:    #121825;
    --color-surface-2:  #1a2030;
    --color-border:     #2a3555;
    --color-gold:       #8ba4c8;
    --color-gold-light: #c0cde0;
    --color-gold-dim:   #4a6080;
    --color-amber:      #6a8cc0;
    --color-text:       #d8dce8;
    --color-text-muted: #6a7890;
    --color-rune-glow:  rgba(139,164,200,0.15);
}

body.theme-dethek {
    --color-bg:         #0f0c09;
    --color-surface:    #1c1814;
    --color-surface-2:  #2a2420;
    --color-border:     #4a3828;
    --color-gold:       #d4722a;
    --color-gold-light: #e8943c;
    --color-gold-dim:   #8a4a18;
    --color-amber:      #ff6a2a;
    --color-text:       #e0d4c4;
    --color-text-muted: #8a7a68;
    --color-rune-glow:  rgba(212,114,42,0.15);
}
```

Because all existing CSS uses `var(--color-*)` already, swapping the class on `<body>` immediately re-themes the entire page — no other CSS changes needed.

**Step 2: Pass theme class from backend**

Add a `themeClass` field to `Script` enum:

```java
ELDER_FUTHARK("Elder Futhark", "Elder Futhark · ...", "theme-futhark"),
TENGWAR("Tengwar", "Tengwar · ...", "theme-tengwar"),
DETHEK("Dethek", "Dethek · D&D Dwarvish runic script · Forgotten Realms", "theme-dethek");
```

Controller passes `selectedScript.getThemeClass()` as a model attribute `themeClass`.

**Step 3: Apply theme class on `<body>`**

In `index.html`:
```html
<body class="spectrum spectrum--dark spectrum--medium" th:classappend="${themeClass}">
```

**Step 4: Swap background glyphs per script**

The `.rune-bg` div currently hardcodes Elder Futhark runes. Make it dynamic:

- Add a `getBackgroundGlyphs()` method to `TransliteratePort` (returns `List<String>` of ~25 characters for the floating background), or
- Simpler: add a `backgroundGlyphs` field to `Script` enum with a string of representative glyphs
- In `index.html`, replace hardcoded `<span>` elements with a `th:each` loop over the glyph list

**Step 5: Apply font class to background glyphs**

Tengwar and Dethek need their custom fonts applied to the floating background glyphs:

```html
<div class="rune-bg" aria-hidden="true"
     th:classappend="${selectedScript.name() == 'TENGWAR'} ? 'tengwar-font' : (${selectedScript.name() == 'DETHEK'} ? 'dethek-font' : '')">
```

**Step 6: JS enhancement — instant theme switch on dropdown change**

Add to `main.js`: when the script `<select>` changes value, immediately swap the `theme-*` class on `<body>` (before form submission). This gives instant visual feedback:

```js
const scriptSelect = document.getElementById('script');
if (scriptSelect) {
    scriptSelect.addEventListener('change', () => {
        const themeMap = {
            'ELDER_FUTHARK': 'theme-futhark',
            'TENGWAR': 'theme-tengwar',
            'DETHEK': 'theme-dethek'
        };
        document.body.className = document.body.className
            .replace(/theme-\w+/g, '')
            .trim() + ' ' + (themeMap[scriptSelect.value] || 'theme-futhark');
    });
}
```

### Testing
- Unit: theme CSS variable overrides render correctly (check with HtmlUnit that `body` has correct `theme-*` class)
- Visual: manually verify each theme in browser at both desktop and mobile breakpoints
- Regression: existing tests should pass unchanged (they don't depend on colors)

### Files Changed
- `src/main/resources/static/css/variables.css` — add theme classes
- `src/main/java/com/druidic/transliterator/core/Script.java` — add `themeClass` field
- `src/main/java/com/druidic/transliterator/adapter/in/web/TransliteratorController.java` — pass `themeClass` to model
- `src/main/resources/templates/index.html` — `th:classappend` on body, dynamic background glyphs
- `src/main/resources/static/js/main.js` — instant theme swap on dropdown change

---

## Feature 2: Dethek (D&D Dwarvish) Script

### What
Add a new transliteration script: Dethek, the runic alphabet used by Dwarves, Giants, and Gnomes in the Forgotten Realms (D&D). Dethek is a simple substitution cipher — each Latin letter maps to one Dethek rune — making it architecturally identical to Elder Futhark.

### Dethek Alphabet

Dethek has 24 base runes covering the common Latin sounds. The mapping (from official Forgotten Realms sources):

| Latin | Dethek Rune Name | Notes |
|-------|-----------------|-------|
| A | A | vowel |
| B | B | |
| C | C | hard C (same as K in some variants) |
| D | D | |
| E | E | vowel |
| F | F | |
| G | G | |
| H | H | |
| I | I | vowel |
| J | J | sometimes merged with G |
| K | K | |
| L | L | |
| M | M | |
| N | N | |
| O | O | vowel |
| P | P | |
| Q | Q | rare, sometimes K |
| R | R | |
| S | S | |
| T | T | |
| U | U | vowel |
| V | V | sometimes merged with F |
| W | W | |
| X | X | rare |
| Y | Y | |
| Z | Z | |

### Font Sourcing

**Approach:** Self-host a Dethek web font (WOFF2/WOFF) in `src/main/resources/static/fonts/`, exactly as Tengwar Annatar is currently hosted.

**Font options (research during implementation):**
1. **Dethek font by Daniel Smith** — the same creator as Tengwar Annatar. Uses a QWERTY-to-Dethek mapping similar to how Tengwar Annatar works. If available, this is the ideal choice for consistency.
2. **Other fan-made Dethek fonts** — several exist in TTF/OTF format and can be converted to WOFF2. The key is to identify the character encoding (which Latin key produces which glyph) and build the mapping table accordingly.

**Font integration:**

In `variables.css`:
```css
@font-face {
    font-family: 'Dethek';
    src: url('/fonts/dethek-webfont.woff2') format('woff2'),
         url('/fonts/dethek-webfont.woff')  format('woff');
    font-weight: normal;
    font-style: normal;
}
```

### Implementation

**Step 1: Source and convert font**

- Find a suitable Dethek TTF/OTF font
- Convert to WOFF2 and WOFF using a tool like `woff2_compress` or an online converter
- Place in `src/main/resources/static/fonts/`
- Determine the character encoding by testing which key produces which glyph

**Step 2: Add `DETHEK` to `Script` enum**

```java
DETHEK("Dethek", "Dethek · D&D Dwarvish runic script · Forgotten Realms", "theme-dethek");
```

**Step 3: Create `DethekTransliterator`**

New file: `src/main/java/com/druidic/transliterator/adapter/out/transliteration/DethekTransliterator.java`

Follows the same pattern as `FutharkTransliterator`:
- `@Qualifier("dethek")` and `@Component`
- Static `Map<Character, String>` with 26 entries (a-z mapped to font encoding characters)
- Stream API for transliteration (identical logic to Futhark)
- `getLegend()` returns ~24-26 `LegendEntry` objects
- Spaces preserved, unmapped characters dropped

```java
@Qualifier("dethek")
@Component
public class DethekTransliterator implements TransliteratePort {

    // Character mappings depend on the font's encoding.
    // Example (if font maps A→a, B→b, etc. with its own glyphs):
    private static final Map<Character, String> DETHEK_MAP = Map.ofEntries(
        Map.entry('a', "a"),  // Dethek 'A' rune
        Map.entry('b', "b"),  // Dethek 'B' rune
        // ... remaining 24 entries determined by font encoding
    );

    @Override
    public TransliterationResult transliterate(TransliterationRequest request) {
        // Identical stream logic to FutharkTransliterator
    }

    @Override
    public List<LegendEntry> getLegend() {
        return List.of(
            // 24-26 entries showing each Dethek rune and its Latin equivalent
        );
    }
}
```

**Step 4: Wire into controller**

Add `@Qualifier("dethek")` parameter to controller constructor and add to the map:

```java
public TransliteratorController(
        @Qualifier("elderFuthark") TransliteratePort elderFutharkTransliterator,
        @Qualifier("tengwar") TransliteratePort tengwarTransliterator,
        @Qualifier("dethek") TransliteratePort dethekTransliterator) {
    this.transliterators = Map.of(
            Script.ELDER_FUTHARK, elderFutharkTransliterator,
            Script.TENGWAR, tengwarTransliterator,
            Script.DETHEK, dethekTransliterator
    );
}
```

**Step 5: Add CSS for Dethek font rendering**

In `components.css`, add Dethek font class alongside Tengwar:

```css
.legend-rune.dethek-font {
    font-family: 'Dethek', serif;
    font-size: 1.8rem;
}
```

In `index.html`, extend the legend conditional to handle Dethek:

```html
<span class="legend-rune"
      th:classappend="${selectedScript.name() == 'TENGWAR'} ? 'tengwar-font' : (${selectedScript.name() == 'DETHEK'} ? 'dethek-font' : '')"
      th:text="${entry.glyph()}"></span>
```

Similarly, the output text needs the Dethek font. Update `.rune-output-text` font stack:
```css
font-family: 'Tengwar Annatar', 'Dethek', 'Noto Sans Runic', var(--font-body), serif;
```

Or better: add a `fontClass` field to `Script` enum and apply it to the output text dynamically.

### Testing

- **Unit tests:** Create `DethekTransliteratorTest.java` following the same pattern as `FutharkTransliteratorTest` — parameterized single-letter tests, spaces, newlines, uppercase, unmapped chars, blank input.
- **Controller test:** Add a test that `DETHEK` script works via POST.
- **Template test:** Verify legend count matches expected Dethek entries.
- **E2E test:** HtmlUnit test submitting with Dethek script.

### Files Changed
- `src/main/resources/static/fonts/dethek-webfont.woff2` — new font file
- `src/main/resources/static/fonts/dethek-webfont.woff` — new font file
- `src/main/resources/static/css/variables.css` — `@font-face` for Dethek
- `src/main/java/com/druidic/transliterator/core/Script.java` — add `DETHEK` enum entry
- `src/main/java/com/druidic/transliterator/adapter/out/transliteration/DethekTransliterator.java` — new file
- `src/main/java/com/druidic/transliterator/adapter/in/web/TransliteratorController.java` — wire Dethek
- `src/main/resources/static/css/components.css` — `.dethek-font` class
- `src/main/resources/templates/index.html` — extend font class conditionals
- `src/test/java/.../DethekTransliteratorTest.java` — new test file

---

## Feature 3: Live Preview

### What
As the user types in the textarea, the output updates in real-time without a page reload. The submit button remains for accessibility and non-JS users, but the primary interaction becomes type-and-see.

### Architecture

**Approach: AJAX to a new REST endpoint**

The existing SSR flow stays intact. We add a lightweight JSON API endpoint that the JS calls on each keystroke (debounced).

Why not client-side transliteration? Because transliteration logic lives in Java (especially Tengwar with its stateful parser). Duplicating it in JS would create a maintenance burden and divergence risk. A 50ms round-trip to localhost is imperceptible.

### Implementation

**Step 1: Create REST endpoint**

New class or add to existing controller. Keeping it in a separate `@RestController` is cleaner:

```java
@RestController
@RequestMapping("/api")
public class TransliterationApiController {

    private final Map<Script, TransliteratePort> transliterators;

    // Same constructor injection as TransliteratorController

    @GetMapping("/transliterate")
    public Map<String, String> transliterate(
            @RequestParam String text,
            @RequestParam(defaultValue = "ELDER_FUTHARK") String script) {

        Script selectedScript = parseScript(script);
        String trimmed = text.length() > 500 ? text.substring(0, 500) : text;

        if (trimmed.isBlank()) {
            return Map.of("runeText", "");
        }

        TransliterationResult result = transliterators.get(selectedScript)
                .transliterate(new TransliterationRequest(trimmed, selectedScript));

        return Map.of("runeText", result.runeText());
    }
}
```

Response: `{ "runeText": "ᚺᛖᛚᛚᛟ" }`

**Step 2: Add JS debounced fetch**

In `main.js`:

```js
// ── Live preview ─────────────────────────────────
const textarea = document.getElementById('inputText');
const scriptSelect = document.getElementById('script');
const form = document.getElementById('transliterator-form');

if (textarea && scriptSelect) {
    let debounceTimer;
    const DEBOUNCE_MS = 250;

    function updatePreview() {
        const text = textarea.value.trim();
        const script = scriptSelect.value;

        if (!text) {
            hideOutput();
            return;
        }

        fetch(`/api/transliterate?text=${encodeURIComponent(text)}&script=${encodeURIComponent(script)}`)
            .then(r => r.json())
            .then(data => {
                if (data.runeText) {
                    showOutput(data.runeText, text, script);
                } else {
                    hideOutput();
                }
            })
            .catch(() => { /* silently fail — user can still use submit button */ });
    }

    textarea.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(updatePreview, DEBOUNCE_MS);
    });

    scriptSelect.addEventListener('change', () => {
        if (textarea.value.trim()) {
            updatePreview();
        }
    });
}
```

**Step 3: Dynamic output section**

The output section is currently rendered server-side only when `result != null`. For live preview, we need to show/hide it client-side:

- Add an empty output section that's hidden by default (CSS `display: none`)
- JS `showOutput()` populates the rune text and makes it visible
- JS `hideOutput()` hides it again
- On page load with existing SSR result, the server-rendered output is shown as before

This means the output HTML needs to exist in the DOM always (not behind `th:if`), but start hidden when there's no server-side result. The `th:if` block becomes a `th:class` that adds a `visible` class when result exists.

**Step 4: Update copy/share buttons for live preview**

The copy and share buttons use `data-text` attributes set server-side. In live preview mode, JS updates these attributes dynamically when the output changes.

### Testing
- **API endpoint test:** MockMvc test for `GET /api/transliterate?text=hello&script=ELDER_FUTHARK` — verify JSON response
- **API edge cases:** blank text returns empty, invalid script falls back, long text truncated
- **JS test (Jest):** debounce fires after delay, fetch called with correct params, output section shown/hidden
- **E2E (HtmlUnit):** This is harder to test with HtmlUnit (no real JS engine). Manual testing or consider adding a Playwright/Selenium test.

### Files Changed
- `src/main/java/com/druidic/transliterator/adapter/in/web/TransliterationApiController.java` — new REST controller
- `src/main/resources/static/js/main.js` — debounced fetch, showOutput/hideOutput
- `src/main/resources/templates/index.html` — output section always in DOM, hidden by default
- `src/test/java/.../TransliterationApiControllerTest.java` — new API test

---

## Feature 4: Session History

### What
A collapsible panel showing recent transliterations from the current browser session. Clicking a history entry re-fills the input and shows the output. History is stored in `sessionStorage` (cleared when the tab closes — no persistence across sessions).

### Design Decisions
- **Client-side only** — no backend changes, no database
- **sessionStorage, not localStorage** — history is ephemeral, tied to the tab
- **Max 20 entries** — FIFO, oldest dropped when full
- **Each entry stores:** input text, script name, rune output, timestamp

### Implementation

**Step 1: Define history data structure**

```js
// Entry shape:
{
    id: crypto.randomUUID(),
    inputText: "hello",
    script: "ELDER_FUTHARK",
    scriptDisplayName: "Elder Futhark",
    runeText: "ᚺᛖᛚᛚᛟ",
    timestamp: 1709000000000
}
```

**Step 2: History manager module**

Create `src/main/resources/static/js/history.js`:

```js
const HISTORY_KEY = 'transliteration_history';
const MAX_ENTRIES = 20;

function getHistory() {
    try {
        return JSON.parse(sessionStorage.getItem(HISTORY_KEY) || '[]');
    } catch {
        return [];
    }
}

function addToHistory(entry) {
    const history = getHistory();
    // Don't add duplicates (same text + script)
    const isDuplicate = history.some(h => h.inputText === entry.inputText && h.script === entry.script);
    if (isDuplicate) return;

    history.unshift(entry);
    if (history.length > MAX_ENTRIES) history.pop();
    sessionStorage.setItem(HISTORY_KEY, JSON.stringify(history));
}

function clearHistory() {
    sessionStorage.removeItem(HISTORY_KEY);
}
```

**Step 3: Save on transliteration**

After a successful transliteration (either form submit or live preview), call `addToHistory()` with the relevant data.

For SSR form submits: read the result from the rendered DOM on page load (if output section is present).
For live preview: save after each successful API response.

**Step 4: History panel UI**

Add a collapsible `<details>` section below the legend (or above the form — TBD, but below legend keeps the main flow clean):

```html
<details class="history-details" id="historyPanel">
    <summary class="history-summary">Recent Translations</summary>
    <div class="history-list" id="historyList">
        <!-- JS populates this -->
    </div>
</details>
```

Each entry rendered as:

```html
<div class="history-entry" data-input="hello" data-script="ELDER_FUTHARK">
    <span class="history-rune">ᚺᛖᛚᛚᛟ</span>
    <span class="history-meta">
        <span class="history-text">"hello"</span>
        <span class="history-script">Elder Futhark</span>
    </span>
</div>
```

**Step 5: Click to restore**

Clicking a history entry:
1. Sets `textarea.value` to the stored input text
2. Sets the script dropdown to the stored script
3. Triggers the live preview (or submits the form if live preview isn't implemented yet)

**Step 6: Clear history button**

Small button inside the history panel: "Clear History" — calls `clearHistory()` and empties the list.

### Styling

History entries use the existing dark surface palette. Rune text shown in the appropriate script font. Hover effect matches legend pair hover (border glow + slight lift).

### Testing
- **JS unit test (Jest):** `addToHistory`, `getHistory`, `clearHistory`, duplicate prevention, FIFO overflow
- **Manual:** verify history persists within tab, clears on tab close, click-to-restore works

### Files Changed
- `src/main/resources/static/js/history.js` — new file
- `src/main/resources/static/js/main.js` — import/call history functions
- `src/main/resources/templates/index.html` — history panel markup
- `src/main/resources/static/css/components.css` — history panel styles

---

## Feature 5: Image Export

### What
A "Download as Image" button that captures the styled output card as a PNG file. The user can save it, share on social media, or use as a wallpaper snippet.

### Approach: Client-Side with html2canvas

**Why client-side?** The output card's styling (CSS gradients, glow effects, custom fonts) is complex. Reproducing it server-side (with Java2D or a headless browser) is fragile. `html2canvas` captures exactly what the user sees.

**Why html2canvas?** It's the most mature, widely-used library for DOM-to-canvas capture. Alternatives like `dom-to-image` exist but are less maintained. `html2canvas` is ~40KB gzipped.

### Implementation

**Step 1: Add html2canvas**

Option A (CDN): Add a `<script>` tag pointing to a CDN (jsDelivr, cdnjs).
Option B (Self-hosted): Download `html2canvas.min.js` to `src/main/resources/static/js/vendor/`.

Self-hosted is preferred for reliability (Render free tier shouldn't depend on external CDN).

**Step 2: Add "Download" button to copy row**

Next to the Copy and Share buttons:

```html
<button type="button"
        class="spectrum-Button spectrum-Button--outline spectrum-Button--secondary spectrum-Button--sizeS download-btn">
    <span class="spectrum-Button-label">⬇ Download PNG</span>
</button>
```

**Step 3: Capture and download logic**

```js
const downloadBtn = document.querySelector('.download-btn');
if (downloadBtn) {
    downloadBtn.addEventListener('click', async () => {
        const card = document.querySelector('.rune-output-card');
        if (!card) return;

        try {
            const canvas = await html2canvas(card, {
                backgroundColor: '#0f0d16',
                scale: 2, // retina quality
                useCORS: true,
                logging: false
            });

            const link = document.createElement('a');
            link.download = 'transliteration.png';
            link.href = canvas.toDataURL('image/png');
            link.click();

            showFeedback('⬇ Image saved!');
        } catch {
            showFeedback('Could not generate image');
        }
    });
}
```

**Step 4: Enhance the exported image**

The raw card capture may lack context. Options:
- Wrap the output card in a styled container that includes the script name and a small watermark ("Druidic Transliterator") — only visible during capture
- Add padding and a background gradient to the captured area
- This can be iterated on — start with raw card capture, improve aesthetics later

### Font Considerations

`html2canvas` may not render custom web fonts correctly in all browsers. Mitigation:
- Ensure fonts are fully loaded before capture (`document.fonts.ready`)
- For Tengwar/Dethek (self-hosted), the font should already be cached
- For Noto Sans Runic (Google Fonts), it should also be cached after first render
- Test across Chrome, Firefox, Safari

### Testing
- **Manual:** click Download, verify PNG is saved with correct content and styling
- **JS unit test:** mock html2canvas, verify it's called with correct element and options

### Files Changed
- `src/main/resources/static/js/vendor/html2canvas.min.js` — new vendor lib
- `src/main/resources/static/js/main.js` — download button handler
- `src/main/resources/templates/index.html` — download button in copy row

---

## Feature 6: Feedback Form

### What
A simple way for users to submit feedback, bug reports, or feature requests. No backend changes — use an external form service.

### Approach: External form link

**Simplest option:** A "Feedback" link in the footer that opens a Google Form or Formspree form in a new tab. Zero backend code, zero maintenance.

**Slightly richer:** An embedded Formspree form in a modal/collapsible panel. Formspree's free tier allows 50 submissions/month — more than enough. No account needed for basic usage.

**Recommended:** Start with a simple footer link to a Google Form. It can be upgraded to an embedded form later if usage warrants it.

### Implementation

**Step 1: Create a Google Form**

Fields:
- Feedback type (dropdown): Bug report, Feature request, General feedback
- Description (long text, required)
- Email (optional, for follow-up)

**Step 2: Add link to footer**

```html
<footer class="site-footer">
    <p th:text="${selectedScript.description}"></p>
    <p class="footer-feedback">
        <a href="https://forms.gle/YOUR_FORM_ID" target="_blank" rel="noopener"
           class="feedback-link">Send Feedback</a>
    </p>
</footer>
```

**Step 3: Style the link**

```css
.footer-feedback { margin-top: 0.5rem; }
.feedback-link {
    color: var(--color-gold-dim);
    text-decoration: none;
    font-style: italic;
    transition: color var(--transition);
}
.feedback-link:hover { color: var(--color-gold); }
```

### Testing
- Visual verification that link appears and opens correct form
- No automated tests needed — it's a static link

### Files Changed
- `src/main/resources/templates/index.html` — feedback link in footer
- `src/main/resources/static/css/components.css` — feedback link styles

---

## Implementation Order & Dependencies

```
Feature 1 (Themed UI) ──┐
                         ├── Feature 2 (Dethek) requires theme colors from Feature 1
Feature 2 (Dethek) ─────┘

Feature 3 (Live Preview) ── standalone, no dependencies
Feature 4 (Session History) ── works with either SSR or live preview
Feature 5 (Image Export) ── needs output to exist (works with either SSR or live preview)
Feature 6 (Feedback Form) ── fully standalone
```

**Recommended execution order:**
1. Feature 1 (Themed UI) — pure CSS + minor backend, immediate visual impact
2. Feature 2 (Dethek) — new content, proves the architecture
3. Feature 3 (Live Preview) — biggest UX improvement
4. Feature 4 (Session History) — complements live preview
5. Feature 5 (Image Export) — fun feature, moderate complexity
6. Feature 6 (Feedback Form) — simplest, do whenever

Each feature is a separate PR/commit. Features 1 and 2 should be done together or sequentially since the Dethek theme is defined in Feature 1. Features 3-6 are fully independent.

---

## Open Questions (to resolve during implementation)

1. **Dethek font sourcing** — Which specific font file to use? Needs research during Feature 2 implementation. Fallback: if no suitable font is found, use Unicode Runic block characters (which overlap with Futhark) or ASCII art representations.

2. **Live preview: always-visible output vs. appear-on-type** — Should the output section be always visible (even empty) or appear only when text is entered? Starting hidden and animating in on first keystroke feels better.

3. **History panel position** — Above or below the legend? Below keeps the primary flow clean. Test both and decide visually.

4. **html2canvas bundle size** — At ~40KB gzipped, it's the only significant JS dependency. Consider lazy-loading it (only download when user clicks "Download PNG" for the first time).
