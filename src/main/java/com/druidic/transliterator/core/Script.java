package com.druidic.transliterator.core;

public enum Script {

    ELDER_FUTHARK("Elder Futhark"),
    TENGWAR("Tengwar");

    private final String displayName;

    Script(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
