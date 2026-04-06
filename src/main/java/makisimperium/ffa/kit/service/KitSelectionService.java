package makisimperium.ffa.kit.service;

import makisimperium.ffa.player.service.PlayerProfileService;

import java.util.Optional;
import java.util.UUID;

public final class KitSelectionService {

    private final KitRegistry kitRegistry;
    private final PlayerProfileService playerProfileService;

    public KitSelectionService(KitRegistry kitRegistry, PlayerProfileService playerProfileService) {
        this.kitRegistry = kitRegistry;
        this.playerProfileService = playerProfileService;
    }

    public String resolveKitForPlayer(UUID playerUniqueId) {
        Optional<String> selectedKit = playerProfileService.getSelectedKit(playerUniqueId)
                .filter(kitId -> kitRegistry.findKit(kitId).isPresent());

        if (selectedKit.isPresent()) {
            return selectedKit.get();
        }

        Optional<String> defaultKit = kitRegistry.getDefaultKit().map(kit -> kit.getKitId());
        defaultKit.ifPresent(kitId -> playerProfileService.setSelectedKit(playerUniqueId, kitId));
        return defaultKit.orElse(null);
    }

    public boolean selectKit(UUID playerUniqueId, String kitId) {
        if (kitRegistry.findKit(kitId).isEmpty()) {
            return false;
        }
        playerProfileService.setSelectedKit(playerUniqueId, kitId);
        return true;
    }
}


