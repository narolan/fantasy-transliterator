# Fantasy Transliterator

A full-stack web application that transliterates English text into ancient and fictional scripts. Currently supports **Elder Futhark** runes and **Tengwar** (Tolkien's Elvish script), with a clean dropdown to switch between them.

Built with Java 17, Spring Boot 3, Thymeleaf (server-side rendering), Gradle, and Adobe Spectrum CSS. Deployable to [Render](https://render.com) via Docker.

🌐 **Live demo:** [fantasy-transliterator.onrender.com](https://fantasy-transliterator.onrender.com/transliterate)

---

## What to expect

Type any English text into the input field, select a script from the dropdown, and hit **Transliterate**. The output renders immediately below in the chosen script.

**Elder Futhark** uses actual Unicode Runic characters (U+16A0–U+16FF) — these render in any modern browser without any extra setup.

**Tengwar** uses the Tengwar Annatar font (included in `static/fonts/`) with Daniel Smith's standard encoding. The font loads automatically — no installation needed.

A few things to know:
- Punctuation is dropped — only letters and spaces are transliterated
- Spaces become runic word separators in Elder Futhark (᛫)
- Tengwar handles digraphs (`th`, `ch`, `sh`, `ph`, `wh`, `ng`, `ck`) as single glyphs, and vowels are written as diacritics above the preceding consonant
- The output can be copied to clipboard with the copy button

---

## Architecture

The project follows **hexagonal architecture** (ports & adapters) with three top-level packages:

```
src/main/java/com/druidic/transliterator/
├── core/                                   # Value objects — zero framework dependencies
│   ├── Script                              # Enum: ELDER_FUTHARK, TENGWAR
│   ├── TransliterationRequest              # Input value object (text + script)
│   └── TransliterationResult              # Output value object (original + transliterated)
├── port/
│   ├── in/
│   │   └── TransliteratePort              # Input port interface (driving side)
│   └── out/
│       └── SaveTransliterationPort        # Output port interface (driven side, unwired)
└── adapter/
    ├── in/
    │   └── web/
    │       └── TransliteratorController   # Thymeleaf controller — depends on port only
    └── out/
        └── transliteration/
            ├── FutharkTransliterator      # Elder Futhark implementation
            └── TengwarTransliterator      # Tengwar implementation (English Mode 6)
```

Adding a new script means adding one class in `adapter/out/transliteration/`, one entry in the `Script` enum, and wiring it in the controller — nothing else changes.

---

## Running locally

**Prerequisites:** Java 17+

```bash
./gradlew bootRun
```

Open [http://localhost:8080](http://localhost:8080).

### With Docker

```bash
docker build -t druidic-transliterator .
docker run -p 8080:8080 druidic-transliterator
```

---

## Deploying to Render

The repo includes a `render.yaml` so Render can configure itself automatically.

1. Push the repo to GitHub
2. Go to [render.com](https://render.com) → **New → Web Service**
3. Connect your GitHub repo
4. Render detects the `render.yaml` and pre-fills all settings
5. Click **Create Web Service** — the first build takes 3–5 minutes

Once live, Render gives you a public URL and automatically redeploys on every push to `main`.

> **Note:** On the free tier, the service sleeps after 15 minutes of inactivity and takes ~30 seconds to wake up on the next request. Upgrade to the Starter plan to keep it always on.

---

## Script reference

### Elder Futhark (~150–800 CE)

Letters without a direct phonetic match are mapped to the nearest sound. Spaces become ᛫.

| Latin | Rune | Name |
|---|---|---|
| A | ᚨ | Ansuz |
| B | ᛒ | Berkano |
| C, K, Q | ᚲ | Kaunan |
| D | ᛞ | Dagaz |
| E | ᛖ | Ehwaz |
| F | ᚠ | Fehu |
| G | ᚷ | Gebo |
| H | ᚺ | Haglaz |
| I | ᛁ | Isaz |
| J, Y | ᛃ | Jera |
| L | ᛚ | Laguz |
| M | ᛗ | Mannaz |
| N | ᚾ | Naudiz |
| O | ᛟ | Othalan |
| P | ᛈ | Pertho |
| R | ᚱ | Raidho |
| S, X | ᛊ | Sowilo |
| T | ᛏ | Tiwaz |
| U, V | ᚢ | Uruz |
| W | ᚹ | Wunjo |
| Z | ᛉ | Algiz |

### Tengwar — English Mode 6

Uses the Tengwar Annatar font by Johan Winge with Daniel Smith's standard encoding. Vowels are tehtar (diacritics) placed above the preceding consonant; standalone vowels sit on a short carrier. Digraphs are mapped to a single tengwa.

| Input | Tengwa |
|---|---|
| t | tinco |
| p | parma |
| c, k, q | quesse |
| d | ando |
| b | umbar |
| f, ph | formen |
| v | ampa |
| g | ungwe |
| n | numen |
| m | malta |
| r | ore |
| l | lambe |
| s | silme |
| z | esse |
| h | hyarmen |
| w | vala |
| y | anna |
| th | súle |
| ng | nwalme |
| sh | harma |
| wh | hwesta |
| ch | calma |