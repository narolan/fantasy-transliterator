/* Fantasy Transliterator — main.js */

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

    // ── Download PNG ─────────────────────────────
    const downloadBtn = document.querySelector('.download-btn');
    if (downloadBtn) {
        downloadBtn.addEventListener('click', async () => {
            const card = document.querySelector('.rune-output-card');
            if (!card || typeof html2canvas === 'undefined') return;

            try {
                await document.fonts.ready;
                const canvas = await html2canvas(card, {
                    backgroundColor: '#0f0d16',
                    scale: 2,
                    useCORS: true,
                    logging: false
                });

                const link = document.createElement('a');
                link.download = 'transliteration.png';
                link.href = canvas.toDataURL('image/png');
                link.click();

                showFeedback('Image saved!');
            } catch {
                showFeedback('Could not generate image');
            }
        });
    }

    // ── History panel ────────────────────────────────
    const historyList = document.getElementById('historyList');
    const historyPanel = document.getElementById('historyPanel');
    const clearHistoryBtn = document.getElementById('clearHistoryBtn');

    function renderHistory() {
        if (!historyList) return;
        const history = getHistory();
        if (history.length === 0) {
            historyList.innerHTML = '<p class="history-empty">No translations yet.</p>';
            return;
        }
        historyList.innerHTML = history.map(h => {
            const truncated = h.inputText.length > 40 ? h.inputText.substring(0, 40) + '…' : h.inputText;
            return `<div class="history-entry" data-input="${h.inputText.replace(/"/g, '&quot;')}" data-script="${h.script}">
                <span class="history-rune ${h.fontClass || ''}">${h.runeText.length > 50 ? h.runeText.substring(0, 50) + '…' : h.runeText}</span>
                <span class="history-meta">
                    <span class="history-text">"${truncated}"</span>
                    <span class="history-script">${h.scriptDisplayName}</span>
                </span>
            </div>`;
        }).join('');
    }

    function saveToHistory(inputText, script, runeText) {
        if (!inputText || !runeText) return;
        const selected = scriptSelect ? scriptSelect.options[scriptSelect.selectedIndex] : null;
        const fontClassMap = { 'ELDER_FUTHARK': '', 'TENGWAR': 'tengwar-font', 'DETHEK': 'dethek-font' };
        addToHistory({
            inputText: inputText,
            script: script,
            scriptDisplayName: selected ? selected.text : script,
            runeText: runeText,
            fontClass: fontClassMap[script] || '',
            timestamp: Date.now()
        });
        renderHistory();
    }

    if (historyList) {
        renderHistory();

        historyList.addEventListener('click', (e) => {
            const entry = e.target.closest('.history-entry');
            if (!entry) return;
            if (textarea) textarea.value = entry.dataset.input;
            if (scriptSelect) {
                scriptSelect.value = entry.dataset.script;
                scriptSelect.dispatchEvent(new Event('change'));
            }
            if (textarea && textarea.value.trim()) {
                updatePreview();
            }
        });
    }

    if (clearHistoryBtn) {
        clearHistoryBtn.addEventListener('click', () => {
            clearHistory();
            renderHistory();
        });
    }

    // Save SSR result to history on page load
    if (outputSection && outputSection.style.display !== 'none' && runeOutputText && textarea) {
        const text = textarea.value.trim();
        const runes = runeOutputText.textContent.trim();
        const script = scriptSelect ? scriptSelect.value : 'ELDER_FUTHARK';
        if (text && runes) saveToHistory(text, script, runes);
    }

    // ── Live preview ────────────────────────────────
    let debounceTimer;
    const DEBOUNCE_MS = 250;

    function updatePreview() {
        const text = textarea ? textarea.value.trim() : '';
        const script = scriptSelect ? scriptSelect.value : 'ELDER_FUTHARK';

        if (!text) {
            hideOutput();
            return;
        }

        fetch(`/api/transliterate?text=${encodeURIComponent(text)}&script=${encodeURIComponent(script)}`)
            .then(r => r.json())
            .then(data => {
                if (data.runeText) {
                    showOutput(data.runeText, text, script);
                    saveToHistory(text, script, data.runeText);
                } else {
                    hideOutput();
                }
            })
            .catch(() => { /* silently fail — user can still use submit button */ });
    }

    if (textarea && scriptSelect) {
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
