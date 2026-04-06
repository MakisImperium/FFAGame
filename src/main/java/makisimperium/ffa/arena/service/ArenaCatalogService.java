package makisimperium.ffa.arena.service;

import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import makisimperium.ffa.arena.model.ArenaDefinition;
import makisimperium.ffa.arena.model.ArenaStatus;
import makisimperium.ffa.arena.model.CuboidZone;
import makisimperium.ffa.arena.model.SpawnPoint;
import makisimperium.ffa.arena.persistence.ArenaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ArenaCatalogService {

    private final PluginBase plugin;
    private final ArenaRepository repository;
    private final Map<String, ArenaDefinition> arenasById = new ConcurrentHashMap<>();

    public ArenaCatalogService(PluginBase plugin, ArenaRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void loadArenas() throws Exception {
        arenasById.clear();
        for (ArenaDefinition arenaDefinition : repository.loadAll()) {
            if (arenaDefinition.getArenaId() == null || arenaDefinition.getArenaId().isBlank()) {
                continue;
            }
            arenasById.put(normalize(arenaDefinition.getArenaId()), arenaDefinition);
        }
    }

    public Collection<ArenaDefinition> listArenas() {
        return arenasById.values();
    }

    public Optional<ArenaDefinition> findArena(String arenaId) {
        if (arenaId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(arenasById.get(normalize(arenaId)));
    }

    public boolean createArena(String arenaId, String worldName) {
        return createArena(arenaId, arenaId, worldName);
    }

    public boolean createArena(String arenaId, String displayName, String worldName) {
        if (arenaId == null || arenaId.isBlank() || worldName == null || worldName.isBlank()) {
            return false;
        }
        String normalizedId = normalize(arenaId);
        if (arenasById.containsKey(normalizedId)) {
            return false;
        }

        String resolvedDisplayName = displayName == null || displayName.isBlank() ? arenaId : displayName.trim();
        ArenaDefinition arenaDefinition = new ArenaDefinition(normalizedId, resolvedDisplayName, worldName.trim());
        if (!persist(arenaDefinition)) {
            return false;
        }

        arenasById.put(normalizedId, arenaDefinition);
        return true;
    }

    public boolean deleteArena(String arenaId) {
        return removeArena(arenaId).isPresent();
    }

    public Optional<ArenaDefinition> removeArena(String arenaId) {
        Optional<ArenaDefinition> arena = findArena(arenaId);
        if (arena.isEmpty()) {
            return Optional.empty();
        }
        try {
            repository.delete(arena.get().getArenaId());
            arenasById.remove(normalize(arenaId));
            return Optional.of(arena.get());
        } catch (Exception exception) {
            plugin.getLogger().error("Failed to delete arena " + arenaId, exception);
            return Optional.empty();
        }
    }

    public boolean restoreArena(ArenaDefinition arenaDefinition) {
        if (arenaDefinition == null || arenaDefinition.getArenaId() == null || arenaDefinition.getArenaId().isBlank()) {
            return false;
        }
        return persistAndUpdate(arenaDefinition);
    }

    public Optional<String> validateActivationReadiness(String arenaId) {
        Optional<ArenaDefinition> arenaOptional = findArena(arenaId);
        if (arenaOptional.isEmpty()) {
            return Optional.of("Arena not found.");
        }
        return validateActivationReadiness(arenaOptional.get());
    }

    public Optional<String> validateActivationReadiness(ArenaDefinition arenaDefinition) {
        if (arenaDefinition.getSpawnPoints().isEmpty()) {
            return Optional.of("No spawn points configured.");
        }
        String worldName = arenaDefinition.getWorldName();
        if (worldName == null || worldName.isBlank()) {
            return Optional.of("World name is missing.");
        }
        if (!plugin.getServer().isLevelLoaded(worldName) && !plugin.getServer().loadLevel(worldName)) {
            return Optional.of("World '" + worldName + "' is not loadable.");
        }
        if (arenaDefinition.getSafeZoneId() != null && !arenaDefinition.getSafeZoneId().isBlank()
                && arenaDefinition.getSafeZone().isEmpty()) {
            return Optional.of("Safe-zone reference is invalid.");
        }
        return Optional.empty();
    }

    public boolean addSpawnPoint(String arenaId, Location location) {
        Optional<ArenaDefinition> arena = findArena(arenaId);
        if (arena.isEmpty()) {
            return false;
        }
        arena.get().getSpawnPoints().add(SpawnPoint.fromLocation(location));
        return persistAndUpdate(arena.get());
    }

    public boolean setArenaStatus(String arenaId, ArenaStatus status) {
        Optional<ArenaDefinition> arena = findArena(arenaId);
        if (arena.isEmpty()) {
            return false;
        }
        if (status == ArenaStatus.ACTIVE) {
            Optional<String> readinessError = validateActivationReadiness(arena.get());
            if (readinessError.isPresent()) {
                return false;
            }
        }
        arena.get().setStatus(status);
        return persistAndUpdate(arena.get());
    }

    public int setAllArenasStatus(ArenaStatus status) {
        int updated = 0;
        List<ArenaDefinition> snapshot = List.copyOf(arenasById.values());
        for (ArenaDefinition arena : snapshot) {
            if (setArenaStatus(arena.getArenaId(), status)) {
                updated++;
            }
        }
        return updated;
    }

    public boolean upsertZone(String arenaId, String zoneId, CuboidZone zone) {
        if (zoneId == null || zoneId.isBlank() || zone == null) {
            return false;
        }
        Optional<ArenaDefinition> arena = findArena(arenaId);
        if (arena.isEmpty()) {
            return false;
        }
        arena.get().upsertZone(zoneId.trim().toLowerCase(Locale.ROOT), zone);
        return persistAndUpdate(arena.get());
    }

    public boolean setSafeZone(String arenaId, String zoneId) {
        Optional<ArenaDefinition> arena = findArena(arenaId);
        if (arena.isEmpty()) {
            return false;
        }

        String normalizedZoneId = zoneId.trim().toLowerCase(Locale.ROOT);
        if (!arena.get().getZones().containsKey(normalizedZoneId)) {
            return false;
        }
        arena.get().setSafeZoneId(normalizedZoneId);
        return persistAndUpdate(arena.get());
    }

    private boolean persistAndUpdate(ArenaDefinition arenaDefinition) {
        if (!persist(arenaDefinition)) {
            return false;
        }
        arenasById.put(normalize(arenaDefinition.getArenaId()), arenaDefinition);
        return true;
    }

    private boolean persist(ArenaDefinition arenaDefinition) {
        try {
            repository.save(arenaDefinition);
            return true;
        } catch (Exception exception) {
            plugin.getLogger().error("Failed to persist arena " + arenaDefinition.getArenaId(), exception);
            return false;
        }
    }

    private String normalize(String arenaId) {
        return arenaId.trim().toLowerCase(Locale.ROOT);
    }
}


