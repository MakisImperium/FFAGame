package makisimperium.ffa.config;

import java.util.Locale;

public enum StorageBackend {
    MYSQL,
    JSON;

    public static StorageBackend fromText(String raw) {
        if (raw == null) {
            return MYSQL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (StorageBackend backend : values()) {
            if (backend.name().equals(normalized)) {
                return backend;
            }
        }
        return MYSQL;
    }
}


