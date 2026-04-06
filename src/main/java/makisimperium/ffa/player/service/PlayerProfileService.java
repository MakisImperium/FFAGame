package makisimperium.ffa.player.service;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import makisimperium.ffa.player.model.PlayerLanguage;
import makisimperium.ffa.player.model.PlayerProfile;
import makisimperium.ffa.player.model.PlayerStatistics;
import makisimperium.ffa.player.storage.PlayerProfileRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PlayerProfileService {

    private final PluginBase plugin;
    private final PlayerProfileRepository repository;
    private final boolean scoreboardEnabledByDefault;
    private final boolean strictDataDependency;
    private final Runnable repositoryUnavailableCallback;
    private final Map<UUID, PlayerProfile> profilesById = new ConcurrentHashMap<>();

    private final Object repositoryLock = new Object();
    private final Map<UUID, PlayerProfile> pendingPersistence = new ConcurrentHashMap<>();
    private final Set<UUID> queuedPersistenceIds = ConcurrentHashMap.newKeySet();
    private final LinkedBlockingQueue<UUID> persistenceQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean workerRunning = new AtomicBoolean(true);
    private final AtomicBoolean outageSignalSent = new AtomicBoolean(false);
    private final Thread persistenceWorker;
    private volatile boolean dataAvailable = true;

    public PlayerProfileService(
            PluginBase plugin,
            PlayerProfileRepository repository,
            boolean scoreboardEnabledByDefault,
            boolean strictDataDependency,
            Runnable repositoryUnavailableCallback
    ) {
        this.plugin = plugin;
        this.repository = repository;
        this.scoreboardEnabledByDefault = scoreboardEnabledByDefault;
        this.strictDataDependency = strictDataDependency;
        this.repositoryUnavailableCallback = repositoryUnavailableCallback;
        this.persistenceWorker = new Thread(this::runPersistenceWorker, "ffa-profile-persistence");
        this.persistenceWorker.setDaemon(true);
        this.persistenceWorker.start();
    }

    public PlayerProfile loadProfile(Player player) {
        UUID uniqueId = player.getUniqueId();
        if (strictDataDependency && !dataAvailable) {
            PlayerProfile fallback = PlayerProfile.createDefault(uniqueId, player.getName(), scoreboardEnabledByDefault);
            fallback.setLastKnownName(player.getName());
            profilesById.put(uniqueId, fallback);
            return fallback;
        }

        PlayerProfile profile;
        try {
            synchronized (repositoryLock) {
                profile = repository.findByUniqueId(uniqueId)
                        .orElseGet(() -> PlayerProfile.createDefault(uniqueId, player.getName(), scoreboardEnabledByDefault));
            }
        } catch (Exception exception) {
            handleRepositoryFailure("load profile for " + player.getName(), exception);
            profile = PlayerProfile.createDefault(uniqueId, player.getName(), scoreboardEnabledByDefault);
        }
        profile.setLastKnownName(player.getName());
        profilesById.put(uniqueId, profile);
        return profile;
    }

    public void unloadProfile(Player player, boolean persist) {
        PlayerProfile profile = profilesById.remove(player.getUniqueId());
        if (persist && profile != null) {
            persistProfile(profile);
        }
    }

    public Optional<PlayerProfile> getProfile(UUID uniqueId) {
        return Optional.ofNullable(profilesById.get(uniqueId));
    }

    public List<PlayerProfile> loadAllProfilesSnapshot() {
        if (strictDataDependency && !dataAvailable) {
            return new ArrayList<>(profilesById.values());
        }

        Map<UUID, PlayerProfile> merged = new HashMap<>();
        try {
            List<PlayerProfile> storedProfiles;
            synchronized (repositoryLock) {
                storedProfiles = repository.loadAllProfiles();
            }
            for (PlayerProfile stored : storedProfiles) {
                merged.put(stored.getUniqueId(), stored);
            }
        } catch (Exception exception) {
            handleRepositoryFailure("load global leaderboard profiles from repository", exception);
        }

        for (PlayerProfile cached : profilesById.values()) {
            merged.put(cached.getUniqueId(), cached);
        }
        return new ArrayList<>(merged.values());
    }

    public boolean isScoreboardEnabled(UUID uniqueId) {
        if (strictDataDependency && !dataAvailable) {
            return false;
        }
        return getProfile(uniqueId)
                .map(profile -> profile.getPreferences().isScoreboardEnabled())
                .orElse(scoreboardEnabledByDefault);
    }

    public boolean setScoreboardEnabled(UUID uniqueId, boolean enabled) {
        if (strictDataDependency && !dataAvailable) {
            return false;
        }
        PlayerProfile profile = profilesById.get(uniqueId);
        if (profile == null) {
            return false;
        }
        profile.getPreferences().setScoreboardEnabled(enabled);
        persistProfile(profile);
        return true;
    }

    public Optional<Boolean> toggleScoreboard(UUID uniqueId) {
        if (strictDataDependency && !dataAvailable) {
            return Optional.empty();
        }
        PlayerProfile profile = profilesById.get(uniqueId);
        if (profile == null) {
            return Optional.empty();
        }
        boolean nextState = !profile.getPreferences().isScoreboardEnabled();
        profile.getPreferences().setScoreboardEnabled(nextState);
        persistProfile(profile);
        return Optional.of(nextState);
    }

    public PlayerLanguage getLanguage(UUID uniqueId) {
        return getProfile(uniqueId)
                .map(profile -> profile.getPreferences().getLanguage())
                .orElse(PlayerLanguage.ENGLISH);
    }

    public boolean setLanguage(UUID uniqueId, PlayerLanguage language) {
        if (strictDataDependency && !dataAvailable) {
            return false;
        }
        PlayerProfile profile = profilesById.get(uniqueId);
        if (profile == null) {
            return false;
        }
        profile.getPreferences().setLanguage(language);
        persistProfile(profile);
        return true;
    }

    public void setSelectedKit(UUID uniqueId, String kitId) {
        if (strictDataDependency && !dataAvailable) {
            return;
        }
        PlayerProfile profile = profilesById.get(uniqueId);
        if (profile == null) {
            return;
        }
        profile.setSelectedKitId(kitId);
        persistProfile(profile);
    }

    public Optional<String> getSelectedKit(UUID uniqueId) {
        return getProfile(uniqueId).map(PlayerProfile::getSelectedKitId);
    }

    public void registerKill(UUID uniqueId) {
        if (strictDataDependency && !dataAvailable) {
            return;
        }
        PlayerProfile profile = profilesById.get(uniqueId);
        if (profile == null) {
            return;
        }
        profile.getStatistics().registerKill();
        persistProfile(profile);
    }

    public void registerDeath(UUID uniqueId) {
        if (strictDataDependency && !dataAvailable) {
            return;
        }
        PlayerProfile profile = profilesById.get(uniqueId);
        if (profile == null) {
            return;
        }
        profile.getStatistics().registerDeath();
        persistProfile(profile);
    }

    public PlayerStatistics getStatisticsOrDefault(UUID uniqueId) {
        return profilesById.getOrDefault(
                uniqueId,
                PlayerProfile.createDefault(uniqueId, "Unknown", scoreboardEnabledByDefault)
        ).getStatistics();
    }

    public void persistAllCachedProfiles() {
        if (strictDataDependency && !dataAvailable) {
            return;
        }
        for (PlayerProfile profile : profilesById.values()) {
            persistProfile(profile);
        }
        flushPendingSynchronously();
    }

    public boolean pingRepository() {
        try {
            synchronized (repositoryLock) {
                boolean reachable = repository.ping();
                if (reachable) {
                    setDataAvailable(true);
                }
                return reachable;
            }
        } catch (Exception exception) {
            handleRepositoryFailure("ping player profile repository", exception);
            return false;
        }
    }

    public boolean isDataAvailable() {
        return dataAvailable;
    }

    public void setDataAvailable(boolean available) {
        this.dataAvailable = available;
        if (available) {
            outageSignalSent.set(false);
        }
    }

    public void closeRepository() {
        workerRunning.set(false);
        persistenceWorker.interrupt();
        flushPendingSynchronously();
        try {
            persistenceWorker.join(1500L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        try {
            synchronized (repositoryLock) {
                repository.close();
            }
        } catch (Exception exception) {
            plugin.getLogger().error("Failed to close player repository", exception);
        }
    }

    private void persistProfile(PlayerProfile profile) {
        if (strictDataDependency && !dataAvailable) {
            return;
        }
        pendingPersistence.put(profile.getUniqueId(), profile);
        UUID uniqueId = profile.getUniqueId();
        if (queuedPersistenceIds.add(uniqueId)) {
            persistenceQueue.offer(uniqueId);
        }
    }

    private void runPersistenceWorker() {
        while (workerRunning.get() || !persistenceQueue.isEmpty()) {
            try {
                UUID uniqueId = persistenceQueue.poll(1, TimeUnit.SECONDS);
                if (uniqueId == null) {
                    continue;
                }
                flushOne(uniqueId);
            } catch (InterruptedException ignored) {
                if (!workerRunning.get()) {
                    break;
                }
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void flushPendingSynchronously() {
        UUID uniqueId;
        while ((uniqueId = persistenceQueue.poll()) != null) {
            flushOne(uniqueId);
        }
        for (UUID pendingId : new ArrayList<>(pendingPersistence.keySet())) {
            flushOne(pendingId);
        }
    }

    private void flushOne(UUID uniqueId) {
        queuedPersistenceIds.remove(uniqueId);
        PlayerProfile profile = pendingPersistence.remove(uniqueId);
        if (profile == null) {
            return;
        }
        try {
            synchronized (repositoryLock) {
                repository.save(profile);
            }
        } catch (Exception exception) {
            handleRepositoryFailure("save profile for " + profile.getLastKnownName(), exception);
        }
    }

    private void handleRepositoryFailure(String action, Exception exception) {
        plugin.getLogger().error("Failed to " + action, exception);
        if (!strictDataDependency) {
            return;
        }

        dataAvailable = false;
        if (!outageSignalSent.compareAndSet(false, true)) {
            return;
        }

        if (repositoryUnavailableCallback != null) {
            try {
                repositoryUnavailableCallback.run();
            } catch (Exception callbackException) {
                plugin.getLogger().error("Failed to trigger repository outage callback", callbackException);
            }
        }
    }
}


