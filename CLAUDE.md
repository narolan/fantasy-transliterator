# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./gradlew bootRun        # Run locally at http://localhost:8080
./gradlew build           # Build the project
./gradlew test            # Run tests (JUnit Platform)
```

Java 17+ required. Spring Boot 3.2.0 with Gradle 8.5. No frontend framework — server-side rendering only.

## Architecture

Hexagonal architecture (ports & adapters) under `src/main/java/com/druidic/transliterator/`:

- **core/** — Pure domain objects with zero framework dependencies: `Script` enum, `TransliterationRequest`, `TransliterationResult`
- **port/in/** — Input port interface (`TransliteratePort`) that adapters implement
- **port/out/** — Output port interface (`SaveTransliterationPort`, currently unwired)
- **adapter/in/web/** — `TransliteratorController` (Spring MVC + Thymeleaf), depends on ports only
- **adapter/out/transliteration/** — Script implementations (`FutharkTransliterator`, `TengwarTransliterator`), each implements `TransliteratePort`

The controller holds a `Map<Script, TransliteratePort>` injected by Spring. Adding a new script requires: one new adapter class implementing `TransliteratePort` with `@Qualifier`, one new `Script` enum entry, and wiring in the controller.

## Transliteration Details

**Elder Futhark:** Stream API character-by-character mapping to Unicode Runic block (U+16A0-U+16FF). Rendered via Noto Sans Runic (Google Fonts). Spaces become runic word separators (᛫). Unmapped characters are dropped.

**Tengwar:** Stateful index-based loop with lookahead — vowels attach to the preceding consonant as diacritics (tehtar), so the parser must buffer state. Uses Tengwar Annatar font (self-hosted in `static/fonts/`, Daniel Smith encoding). Handles digraphs (th, ch, sh, ph, wh, ng, ck, qu), short carriers for standalone vowels, and doubling marks for consecutive identical consonants.

## Frontend

Server-side rendered with Thymeleaf (`src/main/resources/templates/index.html`). Static assets in `src/main/resources/static/` — CSS uses Adobe Spectrum framework with custom dark theme, JS handles copy/share buttons and Ctrl+Enter submit. Share feature: `GET /?text=...&script=...` pre-fills input and auto-transliterates.

## Deployment

Docker on Render, auto-deploys from `main` branch. `render.yaml` included. Live at https://fantasy-transliterator.onrender.com
