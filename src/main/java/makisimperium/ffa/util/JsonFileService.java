package makisimperium.ffa.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class JsonFileService {

    private final Gson gson;

    public JsonFileService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Gson gson() {
        return gson;
    }

    public void ensureDirectory(Path directory) throws IOException {
        Files.createDirectories(directory);
    }

    public <T> Optional<T> read(Path path, Class<T> type) throws IOException {
        if (Files.notExists(path)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(gson.fromJson(reader, type));
        }
    }

    public void write(Path path, Object value) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            gson.toJson(value, writer);
        }
    }
}


