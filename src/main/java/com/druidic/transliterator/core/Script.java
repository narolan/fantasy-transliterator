package com.druidic.transliterator.core;

public enum Script {

    ELDER_FUTHARK("Elder Futhark",
            "Elder Futhark \u00b7 Proto-Germanic runic alphabet \u00b7 ~150 to ~800 CE",
            "theme-futhark",
            "",
            "\u16A0 \u16A2 \u16A6 \u16A8 \u16B1 \u16B2 \u16B7 \u16B9 \u16BA \u16BE \u16C1 \u16C3 \u16C7 \u16C8 \u16C9 \u16CA \u16CF \u16D2 \u16D6 \u16D7 \u16DA \u16DC \u16DE \u16DF \u16E6"),

    TENGWAR("Tengwar",
            "Tengwar \u00b7 Tolkien\u2019s Elvish script \u00b7 English Mode",
            "theme-tengwar",
            "tengwar-font",
            "1 2 3 q w e r t y u a s d f g h j k l z x 5 6 8 9"),

    DETHEK("Dethek",
            "Dethek \u00b7 D&D Dwarvish runic script \u00b7 Forgotten Realms",
            "theme-dethek",
            "dethek-font",
            "a b c d e f g h i j k l m n o p q r s t u v w x y z");

    private final String displayName;
    private final String description;
    private final String themeClass;
    private final String fontClass;
    private final String backgroundGlyphs;

    Script(String displayName, String description, String themeClass, String fontClass, String backgroundGlyphs) {
        this.displayName = displayName;
        this.description = description;
        this.themeClass = themeClass;
        this.fontClass = fontClass;
        this.backgroundGlyphs = backgroundGlyphs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getThemeClass() {
        return themeClass;
    }

    public String getFontClass() {
        return fontClass;
    }

    public String[] getBackgroundGlyphArray() {
        return backgroundGlyphs.split(" ");
    }
}
