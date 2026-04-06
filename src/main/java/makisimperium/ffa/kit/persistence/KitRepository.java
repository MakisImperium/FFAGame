package makisimperium.ffa.kit.persistence;

import makisimperium.ffa.kit.model.KitDefinition;

import java.util.List;
import java.util.Optional;

public interface KitRepository {

    List<KitDefinition> loadAll() throws Exception;

    Optional<KitDefinition> load(String kitId) throws Exception;

    void save(KitDefinition kitDefinition) throws Exception;

    void delete(String kitId) throws Exception;
}


