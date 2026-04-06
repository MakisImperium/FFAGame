package makisimperium.ffa.kit.persistence;

import makisimperium.ffa.kit.model.KitDefinition;
import makisimperium.ffa.util.JsonFileService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class JsonKitRepository implements KitRepository {

    private final JsonFileService jsonFileService;
    private final Path kitDirectory;

    public JsonKitRepository(JsonFileService jsonFileService, Path kitDirectory) throws Exception {
        this.jsonFileService = jsonFileService;
        this.kitDirectory = kitDirectory;
        this.jsonFileService.ensureDirectory(kitDirectory);
    }

    @Override
    public List<KitDefinition> loadAll() throws Exception {
        List<KitDefinition> kits = new ArrayList<>();
        if (Files.notExists(kitDirectory)) {
            return kits;
        }

        try (var paths = Files.list(kitDirectory)) {
            for (Path path : paths.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                jsonFileService.read(path, KitDefinition.class).ifPresent(kits::add);
            }
        }
        return kits;
    }

    @Override
    public Optional<KitDefinition> load(String kitId) throws Exception {
        return jsonFileService.read(fileFor(kitId), KitDefinition.class);
    }

    @Override
    public void save(KitDefinition kitDefinition) throws Exception {
        jsonFileService.write(fileFor(kitDefinition.getKitId()), kitDefinition);
    }

    @Override
    public void delete(String kitId) throws Exception {
        Files.deleteIfExists(fileFor(kitId));
    }

    private Path fileFor(String kitId) {
        String normalized = kitId.trim().toLowerCase(Locale.ROOT);
        return kitDirectory.resolve(normalized + ".json");
    }
}


