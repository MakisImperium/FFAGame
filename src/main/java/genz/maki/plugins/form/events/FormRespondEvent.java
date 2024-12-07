package genz.maki.plugins.form.events;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.battle.BattleBasis;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.form.KitForm;
import genz.maki.plugins.form.MainForm;
import genz.maki.plugins.kits.manager.KitsManager;

import java.io.IOException;

public class FormRespondEvent implements Listener {

    private final FFAConfig ffaConfig;
    private final Main main;
    private final RoomConfig roomConfig;
    private final BattleBasis battleBasis;
    private final MainForm mainframe;
    private final KitsManager kitsManager;
    private final KitForm kitForm;
    private final PlayerFile playerFile;

    public FormRespondEvent(PlayerFile playerFile,KitsManager kitsManager, FFAConfig ffaConfig, Main main, RoomConfig roomConfig, BattleBasis battleBasis, MainForm mainframe, KitForm kitForm) {
        this.ffaConfig = ffaConfig;
        this.main = main;
        this.roomConfig = roomConfig;
        this.battleBasis = battleBasis;
        this.mainframe = mainframe;
        this.kitForm = kitForm;
        this.kitsManager = kitsManager;
        this.playerFile = playerFile;
    }

    /**
     * Handles the player's form response events. This method is invoked when a player responds
     * to a form presented within the game. It manages player actions based on the responses given
     * in various form windows.
     *
     * @param event the PlayerFormRespondedEvent that encapsulates information about the form
     *              response, including the player, the form window they responded to, and their response.
     * @throws IOException if an input or output exception occurs while processing the form response.
     */

    @EventHandler
    public void onRespond(PlayerFormRespondedEvent event) throws IOException {
        Player player = event.getPlayer();
        kitsManager.removePlayerFromList(player);
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
                        kitForm.showKitForm(player);
                    } else if (button.equals(TextFormat.RED + "LEAVE BATTLE")) {
                        main.getLogger().info("Player " + player.getName() + " is leaving the battle.");
                        battleBasis.leaveBattle(player);
                    } else if (button.equals(TextFormat.RED + "EXIT MENU")) {
                        // Exit menu action
                    } else if (button.equals(TextFormat.RED + "Back")) {
                        mainframe.sendMainframe(player);
                    } else if (button.equals(TextFormat.GREEN + "PROFILE")) {
                        mainframe.sendProfilFrame(player);
                    } else if (button.equalsIgnoreCase(TextFormat.GREEN + "CHANGE KIT")) {
                        kitForm.showChangeKitForm(player);
                    } else {
                        main.getLogger().warning("Unknown button action: " + button);
                    }
                } else if(title.equalsIgnoreCase("Change a Kit")){
                    String kitName = ((FormResponseSimple) event.getResponse()).getClickedButton().getText();
                    kitsManager.changePlayerKitFromList(player, kitName);
                    kitsManager.applyKit(player, kitName);
                }else if (title.equalsIgnoreCase("FFA-Admin")) {
                    if (button.equals(TextFormat.RED + "KITS")) {
                        this.kitForm.showKitAdminForm(player);
                    } else if (button.equals("Create New Kit")) {
                        kitForm.createKitForm(player);
                    }
                } else if (title.equalsIgnoreCase("Kits Admin")) {
                    if (button.equals("Create New Kit")) {
                        kitForm.createKitForm(player);
                    } else if (button.equals("Manage Kits")) {
                        kitForm.manageKitsListForm(player);
                    }
                } else if (title.equalsIgnoreCase("Manage all Kits")) {
                    kitForm.manageKitForm(button, player);
                } else if (kitsManager.listKitFiles().stream().anyMatch(kit -> title.equalsIgnoreCase(kit))) {
                    if (button.equalsIgnoreCase(TextFormat.YELLOW + "Change")) {
                        kitForm.showChangeKitItemsForm(title, player);
                    } else if (button.equalsIgnoreCase(TextFormat.RED + "Delete")) {
                        kitForm.showSureToDeleteKitForm(title, player);
                    } else if (button.equalsIgnoreCase(TextFormat.GREEN + "Apply")) {
                        kitsManager.applyKit(player, title);
                    } else if (button.equalsIgnoreCase(TextFormat.GREEN + "Yes")) {
                        kitsManager.removeKit(title, player);
                    } else if (button.equalsIgnoreCase(TextFormat.RED + "No")) {
                        kitForm.manageKitsListForm(player);
                    }
                } else if (title.equalsIgnoreCase("Select a Kit")) {
                    String kitName = ((FormResponseSimple) event.getResponse()).getClickedButton().getText();
                    kitsManager.addPlayerToKitList(player, kitName);
                    kitsManager.applyKit(player, kitName);
                }
                
            }
        } else if (window instanceof FormWindowCustom) {
            String title = ((FormWindowCustom) window).getTitle();
            if (title.equalsIgnoreCase("Create New Kit")) {
                String kitName = ((FormResponseCustom) event.getResponse()).getInputResponse(0);

                kitsManager.createNewKitFile(kitName, player);

                player.sendMessage("Kit " + kitName + " has been created.");
            } else if (kitsManager.listKitFiles().stream().anyMatch(kit -> title.equalsIgnoreCase(kit + " - Change"))) {
                kitsManager.changeKitItems(title.replace(" - Change", ""), player);
            }
        }
    }
}