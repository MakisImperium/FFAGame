package makisimperium.ffa.arena.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ArenaDefinition {

    private String arenaId;
    private String displayName;
    private String worldName;
    private ArenaStatus status = ArenaStatus.WAITING;
    private List<SpawnPoint> spawnPoints = new ArrayList<>();
    private Map<String, CuboidZone> zones = new HashMap<>();
    private String safeZoneId;

    public ArenaDefinition() {
    }

    public ArenaDefinition(String arenaId, String displayName, String worldName) {
        this.arenaId = arenaId;
        this.displayName = displayName;
        this.worldName = worldName;
        this.status = ArenaStatus.WAITING;
        this.spawnPoints = new ArrayList<>();
        this.zones = new HashMap<>();
    }

    public String getArenaId() {
        return arenaId;
    }

    public String getDisplayName() {
        if (displayName == null || displayName.isBlank()) {
            return arenaId;
        }
        return displayName;
    }

    public String getWorldName() {
        return worldName;
    }

    public ArenaStatus getStatus() {
        if (status == null) {
            status = ArenaStatus.WAITING;
        }
        return status;
    }

    public void setStatus(ArenaStatus status) {
        this.status = status;
    }

    public List<SpawnPoint> getSpawnPoints() {
        if (spawnPoints == null) {
            spawnPoints = new ArrayList<>();
        }
        return spawnPoints;
    }

    public Map<String, CuboidZone> getZones() {
        if (zones == null) {
            zones = new HashMap<>();
        }
        return zones;
    }

    public String getSafeZoneId() {
        return safeZoneId;
    }

    public void setSafeZoneId(String safeZoneId) {
        this.safeZoneId = safeZoneId;
    }

    public Optional<CuboidZone> getSafeZone() {
        if (safeZoneId == null || safeZoneId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(getZones().get(safeZoneId));
    }

    public void upsertZone(String zoneId, CuboidZone zone) {
        getZones().put(zoneId, zone);
    }
}


