package makisimperium.ffa.arena.persistence;

import makisimperium.ffa.arena.model.ArenaDefinition;
import makisimperium.ffa.util.JsonFileService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class JsonArenaRepository implements ArenaRepository {

    private final JsonFileService jsonFileService;
    private final Path arenaDirectory;

    public JsonArenaRepository(JsonFileService jsonFileService, Path arenaDirectory) throws Exception {
        this.jsonFileService = jsonFileService;
        this.arenaDirectory = arenaDirectory;
        this.jsonFileService.ensureDirectory(arenaDirectory);
    }

    @Override
    public List<ArenaDefinition> loadAll() throws Exception {
        List<ArenaDefinition> arenas = new ArrayList<>();
        if (Files.notExists(arenaDirectory)) {
            return arenas;
        }

        try (var paths = Files.list(arenaDirectory)) {
            for (Path path : paths.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                jsonFileService.read(path, ArenaDefinition.class).ifPresent(arenas::add);
            }
        }
        return arenas;
    }

    @Override
    public Optional<ArenaDefinition> load(String arenaId) throws Exception {
        return jsonFileService.read(fileFor(arenaId), ArenaDefinition.class);
    }

    @Override
    public void save(ArenaDefinition arenaDefinition) throws Exception {
        jsonFileService.write(fileFor(arenaDefinition.getArenaId()), arenaDefinition);
    }

    @Override
    public void delete(String arenaId) throws Exception {
        Files.deleteIfExists(fileFor(arenaId));
    }

    private Path fileFor(String arenaId) {
        String normalized = arenaId.trim().toLowerCase(Locale.ROOT);
        return arenaDirectory.resolve(normalized + ".json");
    }
}


