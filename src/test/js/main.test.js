const fs = require('fs');
const path = require('path');

const MAIN_JS = fs.readFileSync(
  path.resolve(__dirname, '../../main/resources/static/js/main.js'),
  'utf-8'
);

function buildDOM({ withOutput = false, withShareBtn = false } = {}) {
  document.body.innerHTML = `
    <form id="transliterator-form">
      <textarea id="inputText"></textarea>
      <select id="script"></select>
      <button type="submit" class="translate-btn">Transliterate</button>
    </form>
    ${withOutput ? `
      <section id="output">
        <p class="rune-output-text">ᚺᛖᛚᛚᛟ</p>
        <button type="button" class="copy-btn" data-text="ᚺᛖᛚᛚᛟ">
          <span class="spectrum-Button-label">Copy</span>
        </button>
        ${withShareBtn ? `
          <button type="button" class="share-btn" data-text="hello" data-script="ELDER_FUTHARK">
            <span class="spectrum-Button-label">Share</span>
          </button>
        ` : ''}
        <span id="copyFeedback" aria-live="polite"></span>
      </section>
    ` : ''}
  `;
}

function loadMainJS() {
  eval(MAIN_JS);
  document.dispatchEvent(new Event('DOMContentLoaded'));
}

// ── Copy button ───────────────────────────────────────

describe('Copy button', () => {
  beforeEach(() => {
    buildDOM({ withOutput: true });
    Object.assign(navigator, {
      clipboard: { writeText: jest.fn().mockResolvedValue(undefined) },
    });
  });

  test('click triggers clipboard.writeText with rune text', async () => {
    loadMainJS();
    document.querySelector('.copy-btn').click();
    await new Promise(r => setTimeout(r, 0));

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith('ᚺᛖᛚᛚᛟ');
  });

  test('shows feedback with visible class', async () => {
    loadMainJS();
    document.querySelector('.copy-btn').click();
    await new Promise(r => setTimeout(r, 0));

    const fb = document.getElementById('copyFeedback');
    expect(fb.textContent).toBe('ᚢ Copied to clipboard!');
    expect(fb.classList.contains('visible')).toBe(true);
  });
});

// ── Feedback timeout ──────────────────────────────────

describe('Feedback timeout', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    buildDOM({ withOutput: true });
    document.getElementById('output').scrollIntoView = jest.fn();
    Object.assign(navigator, {
      clipboard: { writeText: jest.fn().mockResolvedValue(undefined) },
    });
  });

  afterEach(() => jest.useRealTimers());

  test('visible class removed after 2200ms', async () => {
    loadMainJS();
    document.querySelector('.copy-btn').click();

    // Flush the async clipboard promise
    await Promise.resolve();
    await Promise.resolve();

    const fb = document.getElementById('copyFeedback');
    expect(fb.classList.contains('visible')).toBe(true);

    jest.advanceTimersByTime(2200);
    expect(fb.classList.contains('visible')).toBe(false);
  });
});

// ── Clipboard fallback ────────────────────────────────

describe('Clipboard fallback', () => {
  beforeEach(() => {
    buildDOM({ withOutput: true });
    Object.assign(navigator, {
      clipboard: { writeText: jest.fn().mockRejectedValue(new Error('denied')) },
    });
    document.execCommand = jest.fn();
  });

  test('falls back to execCommand when clipboard API rejects', async () => {
    loadMainJS();
    document.querySelector('.copy-btn').click();
    await new Promise(r => setTimeout(r, 0));

    expect(document.execCommand).toHaveBeenCalledWith('copy');
    expect(document.getElementById('copyFeedback').textContent).toBe('ᚢ Copied!');
  });
});

// ── Share button ──────────────────────────────────────

describe('Share button', () => {
  beforeEach(() => {
    buildDOM({ withOutput: true, withShareBtn: true });
    Object.assign(navigator, {
      clipboard: { writeText: jest.fn().mockResolvedValue(undefined) },
    });
  });

  test('constructs correct URL with encodeURIComponent', async () => {
    loadMainJS();
    document.querySelector('.share-btn').click();
    await new Promise(r => setTimeout(r, 0));

    const expectedUrl = `${window.location.origin}/?text=${encodeURIComponent('hello')}&script=${encodeURIComponent('ELDER_FUTHARK')}`;
    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(expectedUrl);
  });

  test('shows link copied feedback', async () => {
    loadMainJS();
    document.querySelector('.share-btn').click();
    await new Promise(r => setTimeout(r, 0));

    expect(document.getElementById('copyFeedback').textContent).toBe('⎘ Link copied!');
  });
});

// ── Ctrl+Enter shortcut ──────────────────────────────

describe('Ctrl+Enter shortcut', () => {
  beforeEach(() => {
    buildDOM();
    loadMainJS();
  });

  test('Ctrl+Enter submits form', () => {
    const form = document.getElementById('transliterator-form');
    form.submit = jest.fn();

    const textarea = document.getElementById('inputText');
    textarea.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', ctrlKey: true, bubbles: true }));

    expect(form.submit).toHaveBeenCalled();
  });

  test('plain Enter does not submit form', () => {
    const form = document.getElementById('transliterator-form');
    form.submit = jest.fn();

    const textarea = document.getElementById('inputText');
    textarea.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));

    expect(form.submit).not.toHaveBeenCalled();
  });
});

// ── Scroll to output ─────────────────────────────────

describe('Scroll to output', () => {
  beforeEach(() => jest.useFakeTimers());
  afterEach(() => jest.useRealTimers());

  test('calls scrollIntoView after 150ms delay', () => {
    buildDOM({ withOutput: true });
    const output = document.getElementById('output');
    output.scrollIntoView = jest.fn();

    loadMainJS();
    jest.advanceTimersByTime(150);

    expect(output.scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth', block: 'nearest' });
  });

  test('no error when output section absent', () => {
    buildDOM({ withOutput: false });
    expect(() => {
      loadMainJS();
      jest.advanceTimersByTime(150);
    }).not.toThrow();
  });
});
