package makisimperium.ffa.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import makisimperium.ffa.ui.form.FfaFormController;

public final class FormInteractionListener implements Listener {

    private final FfaFormController formController;

    public FormInteractionListener(FfaFormController formController) {
        this.formController = formController;
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        formController.handleResponse(
                event.getPlayer(),
                event.getFormID(),
                event.getResponse(),
                event.wasClosed()
        );
    }
}


