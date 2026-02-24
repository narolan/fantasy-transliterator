/* Druidic Transliterator — main.js */

document.addEventListener('DOMContentLoaded', () => {

    // ── Feedback helper ───────────────────────────────
    const copyFeedback = document.getElementById('copyFeedback');

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

    // ── Copy runes ──────────────────────────────────
    const copyBtn = document.querySelector('.copy-btn');

    if (copyBtn) {
        copyBtn.addEventListener('click', async () => {
            const text = copyBtn.dataset.text;
            try {
                await navigator.clipboard.writeText(text);
                showFeedback('ᚢ Copied to clipboard!');
            } catch {
                clipboardFallback(text);
                showFeedback('ᚢ Copied!');
            }
        });
    }

    // ── Scroll to output after SSR form submit ──────
    const output = document.getElementById('output');
    if (output) {
        setTimeout(() => output.scrollIntoView({ behavior: 'smooth', block: 'nearest' }), 150);
    }

    // ── Ctrl/Cmd + Enter to submit ──────────────────
    const textarea = document.getElementById('inputText');
    if (textarea) {
        textarea.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                document.getElementById('transliterator-form').submit();
            }
        });
    }

    const shareBtn = document.querySelector('.share-btn');
    if (shareBtn) {
        shareBtn.addEventListener('click', async () => {
            const text   = shareBtn.dataset.text;
            const script = shareBtn.dataset.script;
            const url    = `${window.location.origin}/?text=${encodeURIComponent(text)}&script=${encodeURIComponent(script)}`;

            try {
                await navigator.clipboard.writeText(url);
                showFeedback('⎘ Link copied!');
            } catch {
                clipboardFallback(url);
                showFeedback('⎘ Link copied!');
            }
        });
    }
});
