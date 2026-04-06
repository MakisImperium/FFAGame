package makisimperium.ffa.player.model;

import java.util.Locale;

public enum PlayerLanguage {
    ENGLISH("EN"),
    GERMAN("DE");

    private final String code;

    PlayerLanguage(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PlayerLanguage fromCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return ENGLISH;
        }
        String normalized = rawCode.trim().toUpperCase(Locale.ROOT);
        for (PlayerLanguage value : values()) {
            if (value.code.equals(normalized) || value.name().equals(normalized)) {
                return value;
            }
        }
        return ENGLISH;
    }
}


