package makisimperium.ffa.player.storage;

import makisimperium.ffa.player.model.PlayerProfile;
import makisimperium.ffa.util.JsonFileService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JsonPlayerProfileRepository implements PlayerProfileRepository {

    private final JsonFileService jsonFileService;
    private final Path playerDirectory;

    public JsonPlayerProfileRepository(JsonFileService jsonFileService, Path playerDirectory) throws Exception {
        this.jsonFileService = jsonFileService;
        this.playerDirectory = playerDirectory;
        this.jsonFileService.ensureDirectory(playerDirectory);
    }

    @Override
    public Optional<PlayerProfile> findByUniqueId(UUID uniqueId) throws Exception {
        return jsonFileService.read(fileFor(uniqueId), PlayerProfile.class);
    }

    @Override
    public List<PlayerProfile> loadAllProfiles() throws Exception {
        List<PlayerProfile> profiles = new ArrayList<>();
        if (Files.notExists(playerDirectory)) {
            return profiles;
        }

        try (var paths = Files.list(playerDirectory)) {
            for (Path path : paths.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                jsonFileService.read(path, PlayerProfile.class).ifPresent(profiles::add);
            }
        }
        return profiles;
    }

    @Override
    public void save(PlayerProfile profile) throws Exception {
        jsonFileService.write(fileFor(profile.getUniqueId()), profile);
    }

    private Path fileFor(UUID uniqueId) {
        return playerDirectory.resolve(uniqueId.toString() + ".json");
    }
}


