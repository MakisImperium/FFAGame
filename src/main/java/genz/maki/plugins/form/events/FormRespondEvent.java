package genz.maki.plugins.form.events;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.battle.BattleBasis;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.form.MainForm;

import java.io.IOException;

public class FormRespondEvent implements Listener {

    private final FFAConfig ffaConfig;
    private final Main main;
    private final RoomConfig roomConfig;
    private final BattleBasis battleBasis;
    private final MainForm mainframe;

    public FormRespondEvent(FFAConfig ffaConfig, Main main, RoomConfig roomConfig, BattleBasis battleBasis, MainForm mainframe) {
        this.ffaConfig = ffaConfig;
        this.main = main;
        this.roomConfig = roomConfig;
        this.battleBasis = battleBasis;
        this.mainframe = mainframe;
    }

    @EventHandler
    public void onRespond(PlayerFormRespondedEvent event) throws IOException {
        Player player = event.getPlayer();
        FormWindow window = event.getWindow();

        if (event.getResponse() == null) return;
        if (window instanceof FormWindowSimple) {
            String title = ((FormWindowSimple) window).getTitle();
            String button = ((FormResponseSimple) event.getResponse()).getClickedButton().getText();
            if (!event.wasClosed()) {
                if (title.equalsIgnoreCase(ffaConfig.getFormName())) {
                    if (button.equals(TextFormat.GREEN + "JOIN BATTLE")) {
                        main.getLogger().info("Player " + player.getName() + " is joining the battle.");
                        battleBasis.joinToBattle(player);
                    } else if (button.equals(TextFormat.RED + "LEAVE BATTLE")) {
                        main.getLogger().info("Player " + player.getName() + " is leaving the battle.");
                        battleBasis.leaveBattle(player);
                    } else if (button.equals(TextFormat.RED + "EXIT MENU")) {
                        // Exit menu action
                    } else if (button.equals(TextFormat.RED + "Back")) {
                        mainframe.sendMainframe(player);
                    } else if (button.equals(TextFormat.GREEN + "PROFILE")) {
                        mainframe.sendProfilFrame(player);
                    } else {
                        main.getLogger().warning("Unknown button action: " + button);
                    }
                }
            }
        }
    }
}