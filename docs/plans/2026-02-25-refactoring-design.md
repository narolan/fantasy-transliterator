# Refactoring Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Split large frontend files into focused modules, make legend data-driven so new scripts don't require HTML edits, and add tests as a safety net.

**Architecture:** CSS modularization via `@import`s. Data-driven legends via `LegendEntry` record + `TransliteratePort.getLegend()`. Tests written before each refactoring step for safety.

**Tech Stack:** Java 17, Spring Boot 3.2.0, Thymeleaf, JUnit 5, MockMvc, Gradle 8.5

---

### Task 1: Futhark Transliterator Tests

**Files:**
- Create: `src/test/java/com/druidic/transliterator/adapter/out/transliteration/FutharkTransliteratorTest.java`

**Step 1: Create test directory and write tests**

```java
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
```

**Step 2: Run tests to verify they pass**

Run: `./gradlew test --tests "*FutharkTransliteratorTest" --info`
Expected: All tests PASS (these test existing behavior)

**Step 3: Commit**

```bash
git add src/test/java/com/druidic/transliterator/adapter/out/transliteration/FutharkTransliteratorTest.java
git commit -m "test: add Futhark transliterator unit tests"
```

---

### Task 2: Tengwar Transliterator Tests

**Files:**
- Create: `src/test/java/com/druidic/transliterator/adapter/out/transliteration/TengwarTransliteratorTest.java`

**Step 1: Write tests**

```java
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
        // 'a' alone → short carrier (`) + tehta (#)
        assertEquals("`#", transliterate("a").runeText());
    }

    @Test
    void vowelAfterConsonantAttachesToConsonant() {
        // 't' = "1", 'a' tehta = "#" → "1#"
        assertEquals("1#", transliterate("ta").runeText());
    }

    @Test
    void consecutiveVowelsEachGetCarrier() {
        // 'a' 'e' → `# `$
        assertEquals("`#`$", transliterate("ae").runeText());
    }

    @Test
    void consonantWithNoFollowingVowelFlushesBare() {
        // 't' = "1", then end of string
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
        // "the" → digraph "th"="3", vowel 'e'="#" → "3$"
        assertEquals("3$", transliterate("the").runeText());
    }

    @Test
    void doubledConsonantUsesDoublingMark() {
        // "ll" → consonant 'l'="j" + doubling mark "~"
        assertEquals("j~", transliterate("ll").runeText());
    }

    @Test
    void doubledConsonantWithVowel() {
        // "all" → short carrier+a + l+doubling mark → "`#j~"
        assertEquals("`#j~", transliterate("all").runeText());
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
```

**Step 2: Run tests**

Run: `./gradlew test --tests "*TengwarTransliteratorTest" --info`
Expected: All tests PASS

**Step 3: Commit**

```bash
git add src/test/java/com/druidic/transliterator/adapter/out/transliteration/TengwarTransliteratorTest.java
git commit -m "test: add Tengwar transliterator unit tests"
```

---

### Task 3: Controller Integration Tests

**Files:**
- Create: `src/test/java/com/druidic/transliterator/adapter/in/web/TransliteratorControllerTest.java`

**Step 1: Write tests**

```java
package com.druidic.transliterator.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransliteratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIndexReturnsPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("scripts", "selectedScript"));
    }

    @Test
    void getWithTextParamTransliterates() throws Exception {
        mockMvc.perform(get("/").param("text", "hello").param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("inputText", "hello"));
    }

    @Test
    void postTransliterates() throws Exception {
        mockMvc.perform(post("/").param("inputText", "hello").param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("result"));
    }

    @Test
    void invalidScriptFallsBackToFuthark() throws Exception {
        mockMvc.perform(get("/").param("text", "hello").param("script", "INVALID"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedScript",
                        com.druidic.transliterator.core.Script.ELDER_FUTHARK));
    }

    @Test
    void longInputIsTruncated() throws Exception {
        String longInput = "a".repeat(600);
        mockMvc.perform(post("/").param("inputText", longInput).param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("inputText", "a".repeat(500)));
    }

    @Test
    void blankInputReturnsNoResult() throws Exception {
        mockMvc.perform(post("/").param("inputText", "   ").param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("result", org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void tengwarScriptWorks() throws Exception {
        mockMvc.perform(post("/").param("inputText", "hello").param("script", "TENGWAR"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("result"));
    }
}
```

**Step 2: Run tests**

Run: `./gradlew test --tests "*TransliteratorControllerTest" --info`
Expected: All tests PASS

**Step 3: Commit**

```bash
git add src/test/java/com/druidic/transliterator/adapter/in/web/TransliteratorControllerTest.java
git commit -m "test: add controller integration tests"
```

---

### Task 4: CSS Modularization

**Files:**
- Modify: `src/main/resources/static/css/main.css` (becomes import-only entry point)
- Create: `src/main/resources/static/css/variables.css`
- Create: `src/main/resources/static/css/animations.css`
- Create: `src/main/resources/static/css/layout.css`
- Create: `src/main/resources/static/css/components.css`
- Create: `src/main/resources/static/css/responsive.css`

**Step 1: Create `variables.css`**

Extract lines 1-42 from `main.css`: `@font-face`, `:root` variables, universal reset, `html`, `body.spectrum`.

**Step 2: Create `animations.css`**

Extract lines 44-91: `.rune-bg`, all `.rune-bg span:nth-child(N)` rules, `@keyframes floatRune`.

**Step 3: Create `layout.css`**

Extract lines 93-175: `.page-wrapper`, `.site-header`, `.header-ornament`, `.rune-accent`, `@keyframes runeGlow`, `.divider-line`, `.site-title`, `.site-subtitle`, `.main-card`, `.main-card::before`.

**Step 4: Create `components.css`**

Extract lines 177-453: `.input-section`, `.spectrum-FieldLabel`, `.spectrum-Textfield-input`, `.field-hint`, `.script-section`, `.script-select-wrapper`, `.script-select`, `.select-arrow`, `.button-row`, `.translate-btn`, `.clear-btn`, `.result-divider`, `.divider-rune`, `.hr-line`, `.output-section`, `@keyframes fadeSlideIn`, `.rune-output-card`, `.rune-output-glow`, `@keyframes glowPulse`, `.rune-output-text`, `.copy-row`, `.copy-btn`, `.share-btn`, `.copy-feedback`, `.legend-details` through `.legend-letter`, `.site-footer`.

**Step 5: Create `responsive.css`**

Extract lines 458-473: both `@media` queries.

**Step 6: Replace `main.css` with imports**

```css
/* Druidic Transliterator — main.css (entry point) */
@import url('variables.css');
@import url('animations.css');
@import url('layout.css');
@import url('components.css');
@import url('responsive.css');
```

**Step 7: Run the app and verify visually**

Run: `./gradlew bootRun`
Open: `http://localhost:8080`
Verify: Page looks identical. Check both Futhark and Tengwar scripts. Check mobile viewport.

**Step 8: Run all tests to confirm no regression**

Run: `./gradlew test`
Expected: All tests PASS

**Step 9: Commit**

```bash
git add src/main/resources/static/css/
git commit -m "refactor: split main.css into modular files"
```

---

### Task 5: LegendEntry Record + Interface Change

**Files:**
- Create: `src/main/java/com/druidic/transliterator/core/LegendEntry.java`
- Modify: `src/main/java/com/druidic/transliterator/port/in/TransliteratePort.java`

**Step 1: Create `LegendEntry`**

```java
package com.druidic.transliterator.core;

public record LegendEntry(String glyph, String label) {}
```

**Step 2: Add `getLegend()` to `TransliteratePort`**

```java
package com.druidic.transliterator.port.in;

import com.druidic.transliterator.core.LegendEntry;
import com.druidic.transliterator.core.TransliterationRequest;
import com.druidic.transliterator.core.TransliterationResult;

import java.util.List;

public interface TransliteratePort {
    TransliterationResult transliterate(TransliterationRequest request);
    List<LegendEntry> getLegend();
}
```

**Step 3: Build to confirm compilation fails**

Run: `./gradlew build`
Expected: FAIL — both transliterators don't implement `getLegend()` yet. This confirms the interface change propagates.

**Step 4: Commit**

```bash
git add src/main/java/com/druidic/transliterator/core/LegendEntry.java src/main/java/com/druidic/transliterator/port/in/TransliteratePort.java
git commit -m "feat: add LegendEntry record and getLegend() to TransliteratePort"
```

---

### Task 6: Implement getLegend() in Both Transliterators

**Files:**
- Modify: `src/main/java/com/druidic/transliterator/adapter/out/transliteration/FutharkTransliterator.java`
- Modify: `src/main/java/com/druidic/transliterator/adapter/out/transliteration/TengwarTransliterator.java`

**Step 1: Add `getLegend()` to `FutharkTransliterator`**

Add import for `LegendEntry` and `List`, then add method:

```java
@Override
public List<LegendEntry> getLegend() {
    return List.of(
        new LegendEntry("ᚠ", "F"),     new LegendEntry("ᚢ", "U/V"),
        new LegendEntry("ᚦ", "TH"),    new LegendEntry("ᚨ", "A"),
        new LegendEntry("ᚱ", "R"),     new LegendEntry("ᚲ", "K/C/Q"),
        new LegendEntry("ᚷ", "G"),     new LegendEntry("ᚹ", "W"),
        new LegendEntry("ᚺ", "H"),     new LegendEntry("ᚾ", "N"),
        new LegendEntry("ᛁ", "I"),     new LegendEntry("ᛃ", "J/Y"),
        new LegendEntry("ᛈ", "P"),     new LegendEntry("ᛉ", "Z"),
        new LegendEntry("ᛊ", "S/X"),   new LegendEntry("ᛏ", "T"),
        new LegendEntry("ᛒ", "B"),     new LegendEntry("ᛖ", "E"),
        new LegendEntry("ᛗ", "M"),     new LegendEntry("ᛚ", "L"),
        new LegendEntry("ᛜ", "NG"),    new LegendEntry("ᛞ", "D"),
        new LegendEntry("ᛟ", "O")
    );
}
```

**Step 2: Add `getLegend()` to `TengwarTransliterator`**

Add import for `LegendEntry` and `List`, then add method:

```java
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
```

**Step 3: Build and run all tests**

Run: `./gradlew test`
Expected: All tests PASS, build succeeds.

**Step 4: Commit**

```bash
git add src/main/java/com/druidic/transliterator/adapter/out/transliteration/FutharkTransliterator.java src/main/java/com/druidic/transliterator/adapter/out/transliteration/TengwarTransliterator.java
git commit -m "feat: implement getLegend() in both transliterators"
```

---

### Task 7: Add description to Script Enum

**Files:**
- Modify: `src/main/java/com/druidic/transliterator/core/Script.java`

**Step 1: Add `description` field**

```java
package com.druidic.transliterator.core;

public enum Script {

    ELDER_FUTHARK("Elder Futhark", "Elder Futhark \u00b7 Proto-Germanic runic alphabet \u00b7 ~150 to ~800 CE"),
    TENGWAR("Tengwar", "Tengwar \u00b7 Tolkien\u2019s Elvish script \u00b7 English Mode");

    private final String displayName;
    private final String description;

    Script(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
```

**Step 2: Run all tests**

Run: `./gradlew test`
Expected: All tests PASS

**Step 3: Commit**

```bash
git add src/main/java/com/druidic/transliterator/core/Script.java
git commit -m "feat: add description field to Script enum"
```

---

### Task 8: Wire Legend Data Through Controller and Template

**Files:**
- Modify: `src/main/java/com/druidic/transliterator/adapter/in/web/TransliteratorController.java`
- Modify: `src/main/resources/templates/index.html`

**Step 1: Pass legend data in controller**

In `handleTransliteration()`, after `model.addAttribute("selectedScript", selectedScript);` add:

```java
model.addAttribute("legend", transliterators.get(selectedScript).getLegend());
```

**Step 2: Replace hardcoded legends in `index.html`**

Replace lines 141-196 (both `<details>` blocks) with:

```html
        <details class="legend-details">
            <summary class="legend-summary"
                     th:text="${selectedScript.displayName + ' Reference'}">Reference</summary>
            <div class="legend-grid"
                 th:classappend="${selectedScript.name() == 'TENGWAR'} ? 'tengwar-legend'">
                <div class="legend-pair" th:each="entry : ${legend}">
                    <span class="legend-rune"
                          th:classappend="${selectedScript.name() == 'TENGWAR'} ? 'tengwar-font'"
                          th:text="${entry.glyph()}"></span>
                    <span class="legend-letter" th:text="${entry.label()}"></span>
                </div>
            </div>
        </details>
```

**Step 3: Replace hardcoded footer in `index.html`**

Replace lines 198-201 (the footer with two `th:if` blocks) with:

```html
        <footer class="site-footer">
            <p th:text="${selectedScript.description}"></p>
        </footer>
```

**Step 4: Run all tests**

Run: `./gradlew test`
Expected: All tests PASS

**Step 5: Run the app and verify visually**

Run: `./gradlew bootRun`
Open: `http://localhost:8080`
Verify: Legend renders correctly for both Futhark and Tengwar. Footer text displays correctly. Switch between scripts and confirm.

**Step 6: Commit**

```bash
git add src/main/java/com/druidic/transliterator/adapter/in/web/TransliteratorController.java src/main/resources/templates/index.html
git commit -m "refactor: make legends and footer data-driven from transliterator ports"
```

---

### Task 9: Update Controller Tests for Legend

**Files:**
- Modify: `src/test/java/com/druidic/transliterator/adapter/in/web/TransliteratorControllerTest.java`

**Step 1: Add legend assertion to existing test**

Add a new test:

```java
@Test
void modelContainsLegendData() throws Exception {
    mockMvc.perform(get("/").param("text", "hello").param("script", "ELDER_FUTHARK"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("legend"));
}
```

**Step 2: Run all tests**

Run: `./gradlew test`
Expected: All tests PASS

**Step 3: Commit**

```bash
git add src/test/java/com/druidic/transliterator/adapter/in/web/TransliteratorControllerTest.java
git commit -m "test: add legend model attribute assertion to controller tests"
```

---

### Task 10: Final Verification

**Step 1: Run full test suite**

Run: `./gradlew test`
Expected: All tests PASS

**Step 2: Run app and do full manual check**

Run: `./gradlew bootRun`
Verify:
- Homepage loads with correct styling
- Futhark transliteration works, legend shows all runes
- Tengwar transliteration works, legend shows all tengwar glyphs in correct font
- Footer shows correct description for each script
- Copy and Share buttons work
- Mobile viewport looks correct
- Ctrl+Enter submits
- Share URL pre-fills and auto-transliterates

**Step 3: Final commit if any cleanup needed**
