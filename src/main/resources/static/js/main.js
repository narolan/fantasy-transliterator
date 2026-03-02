/* Druidic Transliterator — main.js */

document.addEventListener('DOMContentLoaded', () => {

    // ── DOM references ──────────────────────────────
    const copyFeedback = document.getElementById('copyFeedback');
    const textarea = document.getElementById('inputText');
    const scriptSelect = document.getElementById('script');
    const outputSection = document.getElementById('output');
    const resultDivider = document.getElementById('resultDivider');
    const runeOutputText = document.getElementById('runeOutputText');
    const outputLabel = document.getElementById('outputLabel');
    const copyBtn = document.querySelector('.copy-btn');
    const shareBtn = document.querySelector('.share-btn');

    // ── Feedback helper ───────────────────────────────
    function showFeedback(msg) {
        if (!copyFeedback) return;
        copyFeedback.textContent = msg;
        copyFeedback.classList.add('visible');
        setTimeout(() => copyFeedback.classList.remove('visible'), 2200);
    }

    function clipboardFallback(text) {
        const ta = document.createElement('textarea');
        ta.value = text;
        ta.style.cssText = 'position:fixed;opacity:0';
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
    }

    // ── Output show/hide ────────────────────────────
    function showOutput(runeText, inputText, script) {
        if (!outputSection || !resultDivider || !runeOutputText) return;

        runeOutputText.textContent = runeText;
        resultDivider.style.display = '';
        outputSection.style.display = '';

        // Update copy button data
        if (copyBtn) copyBtn.dataset.text = runeText;
        if (shareBtn) {
            shareBtn.dataset.text = inputText;
            shareBtn.dataset.script = script;
        }

        // Update output label
        if (outputLabel && scriptSelect) {
            const selected = scriptSelect.options[scriptSelect.selectedIndex];
            outputLabel.textContent = selected.text + ' Output';
        }

        // Apply correct font class to output text
        const fontClassMap = {
            'ELDER_FUTHARK': '',
            'TENGWAR': 'tengwar-font',
            'DETHEK': 'dethek-font'
        };
        runeOutputText.className = runeOutputText.className
            .replace(/\b(tengwar-font|dethek-font)\b/g, '').trim();
        const fontClass = fontClassMap[script];
        if (fontClass) runeOutputText.classList.add(fontClass);
    }

    function hideOutput() {
        if (outputSection) outputSection.style.display = 'none';
        if (resultDivider) resultDivider.style.display = 'none';
    }

    // ── Copy runes ──────────────────────────────────
    if (copyBtn) {
        copyBtn.addEventListener('click', async () => {
            const text = copyBtn.dataset.text;
            try {
                await navigator.clipboard.writeText(text);
                showFeedback('\u16A2 Copied to clipboard!');
            } catch {
                clipboardFallback(text);
                showFeedback('\u16A2 Copied!');
            }
        });
    }

    // ── Scroll to output after SSR form submit ──────
    if (outputSection && outputSection.style.display !== 'none') {
        setTimeout(() => outputSection.scrollIntoView({ behavior: 'smooth', block: 'nearest' }), 150);
    }

    // ── Ctrl/Cmd + Enter to submit ──────────────────
    if (textarea) {
        textarea.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                document.getElementById('transliterator-form').submit();
            }
        });
    }

    // ── Instant theme switch on script change ─────
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

    // ── Share button ─────────────────────────────
    if (shareBtn) {
        shareBtn.addEventListener('click', async () => {
            const text   = shareBtn.dataset.text;
            const script = shareBtn.dataset.script;
            const url    = `${window.location.origin}/?text=${encodeURIComponent(text)}&script=${encodeURIComponent(script)}`;

            try {
                await navigator.clipboard.writeText(url);
                showFeedback('\u238E Link copied!');
            } catch {
                clipboardFallback(url);
                showFeedback('\u238E Link copied!');
            }
        });
    }

    // ── Live preview ────────────────────────────────
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
                        showOutput(data.runeText, textarea.value.trim(), script);
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
});
