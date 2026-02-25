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
