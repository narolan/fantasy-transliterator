# Druidic Transliterator

A Spring Boot web application that transliterates English text into Elder Futhark runes. Built with Java 17, Spring Boot 3, Thymeleaf, and Adobe Spectrum CSS.

## Architecture

The project follows **hexagonal architecture** (ports & adapters):

```
src/main/java/com/druidic/transliterator/
├── core/                          # Domain value objects — no framework deps
│   ├── TransliterationRequest
│   └── TransliterationResult
├── port/
│   ├── in/
│   │   └── TransliteratePort      # Input port (interface)
│   └── out/
│       └── SaveTransliterationPort # Output port (interface, unwired)
└── adapter/
    ├── in/
    │   └── web/
    │       └── TransliteratorController  # Thymeleaf controller
    └── out/
        └── transliteration/
            └── FutharkTransliterator     # Elder Futhark logic
```

## Running locally

**Prerequisites:** Java 17+, Docker (optional)

### With Gradle

```bash
./gradlew bootRun
```

Open [http://localhost:8080](http://localhost:8080).

### With Docker

```bash
docker build -t druidic-transliterator .
docker run -p 8080:8080 druidic-transliterator
```

## Deploying to Render

The repo includes a `render.yaml` for [Render](https://render.com) Blueprint deploys.

1. Push this repo to GitHub.
2. In Render, go to **New → Blueprint** and connect your GitHub repo.
3. Render will detect `render.yaml` and configure the service automatically.
4. The first deploy builds the Docker image and starts the service — typically takes 3–5 minutes.

Alternatively, create the service manually via **New → Web Service**, point it at your repo, and set:

| Setting | Value |
|---|---|
| Runtime | Docker |
| Dockerfile path | `./Dockerfile` |
| Port | `8080` |

## Rune mapping

The transliterator maps each Latin letter to its closest Elder Futhark equivalent (~150–800 CE). Letters without a direct phonetic match (C, Q, V, X, Y) are mapped to the nearest sound. Spaces become the runic word separator ᛫, and punctuation is dropped.

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
