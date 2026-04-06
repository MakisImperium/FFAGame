package makisimperium.ffa.player.storage;

import makisimperium.ffa.player.model.PlayerProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerProfileRepository extends AutoCloseable {

    Optional<PlayerProfile> findByUniqueId(UUID uniqueId) throws Exception;

    List<PlayerProfile> loadAllProfiles() throws Exception;

    void save(PlayerProfile profile) throws Exception;

    default boolean ping() throws Exception {
        return true;
    }

    @Override
    default void close() throws Exception {
    }
}


