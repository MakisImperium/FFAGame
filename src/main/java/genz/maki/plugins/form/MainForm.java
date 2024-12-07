package genz.maki.plugins.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import genz.maki.plugins.kits.manager.KitsManager;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;
import genz.maki.plugins.kits.manager.KitsManager;

import java.io.IOException;

public class MainForm {

    private final FFAConfig ffaConfig;
    private final PlayerFile playerFile;
    private final RoomConfig roomConfig;
    private final Main main;
    private final KitsManager kitsManager;
    private final String FORMNAME;


    public MainForm(Main main, FFAConfig ffaConfig, PlayerFile playerFile, RoomConfig roomConfig, KitsManager kitsManager) {
        this.main = main;
        this.ffaConfig = ffaConfig;
        this.playerFile = playerFile;
        this.roomConfig = roomConfig;
        this.kitsManager = kitsManager;
        this.FORMNAME = ffaConfig.getFormName();
    }

    /**
     * Sends a form window to the specified player, allowing them to interact with the mainframe interface.
     * The interface varies depending on whether the player is already in the "players-playing" list.
     * If the player is in the list, options to "LEAVE BATTLE", "PROFILE", and "EXIT MENU" are presented.
     * Otherwise, options to "JOIN BATTLE", "PROFILE", and "EXIT MENU" are shown.
     *
     * @param player The player to whom the mainframe form is being sent.
     * @throws IOException If an I/O error occurs while checking the player's status in the "players-playing" list.
     */
    public void sendMainframe(Player player) throws IOException {
        FormWindowSimple mainframe = new FormWindowSimple(FORMNAME, ffaConfig.getFormDescription());

        if (roomConfig.isPlayerInList(player)) {
            mainframe.addButton(new ElementButton(TextFormat.RED + "LEAVE BATTLE"));
            mainframe.addButton(new ElementButton(TextFormat.GREEN + "CHANGE KIT"));
            mainframe.addButton(new ElementButton(TextFormat.GREEN + "PROFILE"));
            mainframe.addButton(new ElementButton(TextFormat.RED + "EXIT MENU"));

            player.showFormWindow(mainframe);
        } else {
            mainframe.addButton(new ElementButton(TextFormat.GREEN + "JOIN BATTLE"));
            mainframe.addButton(new ElementButton(TextFormat.GREEN + "PROFILE"));
            mainframe.addButton(new ElementButton(TextFormat.GREEN + "EXIT MENU"));

            player.showFormWindow(mainframe);
        }
    }

    /**
     * Presents a profile settings and statistics interface to the player.
     * This method creates a simple form window displaying the player's current kills, deaths,
     * and kill/death ratio, and a button that allows the player to navigate back.
     *
     * @param player the player for whom the profile frame is being sent
     * @throws IOException if an I/O error occurs during the retrieval of the player's statistics
     */
    public void sendProfilFrame(Player player) throws IOException {
        FormWindowSimple profilframe = new FormWindowSimple(ffaConfig.getFormName(), "Here are your profile settings and statistics.");
        main.getLogger().info(String.valueOf(playerFile.getKills(player)));
        profilframe.setContent(TextFormat.LIGHT_PURPLE + "Current Kills:" + TextFormat.BLUE + " " + String.valueOf(playerFile.getKills(player)));
        profilframe.setContent(TextFormat.LIGHT_PURPLE + "Current Deaths:" + TextFormat.BLUE + " " + String.valueOf(playerFile.getDeaths(player)));
        profilframe.setContent(TextFormat.LIGHT_PURPLE + "Current K/D:" + TextFormat.BLUE + " " + playerFile.getKillDeathRatio(player) + "\n"
            + TextFormat.LIGHT_PURPLE + "Current Kills:" + TextFormat.BLUE + " " + String.valueOf(playerFile.getKills(player))
                + TextFormat.LIGHT_PURPLE + "Current Deaths:" + TextFormat.BLUE + " " + String.valueOf(playerFile.getDeaths(player))
        );
        profilframe.addButton(new ElementButton(TextFormat.RED + "Back"));
        player.showFormWindow(profilframe);
    }

    /**
     * Sends an administrative form to the specified player. This form serves as the
     * admin control panel, allowing players with the appropriate permission to access
     * administrative features such as managing kits.
     *
     * @param player the player to whom the administrative form will be shown.
     *               The player must have "ffagame.admin" permission to view the form.
     * @throws IOException if an input or output exception occurs while showing the form.
     */
    public void sendAdminForm(Player player) throws IOException {
        if (player.hasPermission("ffagame.admin")) {
            FormWindowSimple adminForm = new FormWindowSimple("FFA-Admin", "Admin Control Panel");
            adminForm.addButton(new ElementButton(TextFormat.RED + "KITS"));

            player.showFormWindow(adminForm);
        }
    }

    /**
     * Sends a form to the specified player for selecting or creating kits.
     *
     * @param player the player to whom the kits selection form is displayed
     * @throws IOException if an input/output error occurs while showing the form
     */
    public void sendKitsForm(Player player) throws IOException {
        FormWindowSimple kitsForm = new FormWindowSimple("Kits", "Select a kit or create a new one.");

        for (String kitName : kitsManager.listKitFiles()) {
            kitsForm.addButton(new ElementButton(kitName));
        }

        kitsForm.addButton(new ElementButton("Create Kit"));

        player.showFormWindow(kitsForm);
    }

}
