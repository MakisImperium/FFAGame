package makisimperium.ffa.arena.persistence;

import makisimperium.ffa.arena.model.ArenaDefinition;

import java.util.List;
import java.util.Optional;

public interface ArenaRepository {

    List<ArenaDefinition> loadAll() throws Exception;

    Optional<ArenaDefinition> load(String arenaId) throws Exception;

    void save(ArenaDefinition arenaDefinition) throws Exception;

    void delete(String arenaId) throws Exception;
}


