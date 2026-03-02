/* Fantasy Transliterator — history.js */

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
    const isDuplicate = history.some(h => h.inputText === entry.inputText && h.script === entry.script);
    if (isDuplicate) return;

    history.unshift(entry);
    if (history.length > MAX_ENTRIES) history.pop();
    sessionStorage.setItem(HISTORY_KEY, JSON.stringify(history));
}

function clearHistory() {
    sessionStorage.removeItem(HISTORY_KEY);
}
