package makisimperium.ffa.arena.model;

import java.util.Locale;
import java.util.Optional;

public enum ArenaStatus {
    WAITING,
    ACTIVE,
    MAINTENANCE,
    DISABLED;

    public static Optional<ArenaStatus> fromText(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (ArenaStatus status : values()) {
            if (status.name().equals(normalized)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}


