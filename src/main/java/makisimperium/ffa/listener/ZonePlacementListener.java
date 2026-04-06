package makisimperium.ffa.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import makisimperium.ffa.arena.service.SafeZonePlacementService;
import makisimperium.ffa.ui.form.FfaFormController;

public final class ZonePlacementListener implements Listener {

    private final SafeZonePlacementService safeZonePlacementService;
    private final FfaFormController formController;

    public ZonePlacementListener(SafeZonePlacementService safeZonePlacementService, FfaFormController formController) {
        this.safeZonePlacementService = safeZonePlacementService;
        this.formController = formController;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        SafeZonePlacementService.PlacementResult result = safeZonePlacementService.handleBlockInteraction(event);
        if (result == SafeZonePlacementService.PlacementResult.COMPLETED_SUCCESS
                || result == SafeZonePlacementService.PlacementResult.COMPLETED_FAILED) {
            formController.openAdminZoneMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        safeZonePlacementService.cancel(event.getPlayer().getUniqueId());
    }
}


