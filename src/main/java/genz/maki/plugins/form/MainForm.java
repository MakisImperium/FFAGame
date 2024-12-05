package genz.maki.plugins.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.config.FFAConfig;
import genz.maki.plugins.config.PlayerFile;
import genz.maki.plugins.config.RoomConfig;

import java.io.IOException;

public class MainForm {

    private final FFAConfig ffaConfig;
    private final PlayerFile playerFile;
    private final RoomConfig roomConfig;
    private final Main main;
    private final String FORMNAME;


    public MainForm(Main main, FFAConfig ffaConfig, PlayerFile playerFile, RoomConfig roomConfig) {
        this.main = main;
        this.ffaConfig = ffaConfig;
        this.playerFile = playerFile;
        this.roomConfig = roomConfig;
        this.FORMNAME = ffaConfig.getFormName();
    }

    public void sendMainframe(Player player) throws IOException {
        FormWindowSimple mainframe = new FormWindowSimple(FORMNAME, ffaConfig.getFormDescription());

        if (roomConfig.isPlayerInList(player)) {
            mainframe.addButton(new ElementButton(TextFormat.RED + "LEAVE BATTLE"));
            mainframe.addButton(new ElementButton(TextFormat.GREEN + "PROFILE"));
            mainframe.addButton(new ElementButton(TextFormat.RED + "EXIT MENU"));

            player.showFormWindow(mainframe);
        } else {
            mainframe.addButton(new ElementButton(TextFormat.GREEN + "JOIN BATTLE"));
            mainframe.addButton(new ElementButton(TextFormat.GREEN + "PROFILE"));
            mainframe.addButton(new ElementButton( TextFormat.GREEN + "EXIT MENU"));

            player.showFormWindow(mainframe);
        }
    }

    public void sendProfilFrame(Player player) throws IOException {
        FormWindowSimple profilframe = new FormWindowSimple(ffaConfig.getFormName(), "Here are your profile settings and statistics.");
        profilframe.setContent(TextFormat.LIGHT_PURPLE + "Current Kills:" + TextFormat.BLUE + " " + playerFile.getKills(player));
        profilframe.setContent(TextFormat.LIGHT_PURPLE + "Current Deaths:" + TextFormat.BLUE + " " + playerFile.getDeaths(player));
        profilframe.setContent(TextFormat.LIGHT_PURPLE + "Current K/D:" + TextFormat.BLUE + " " + playerFile.getKillDeathRatio(player));
        profilframe.addButton(new ElementButton(TextFormat.RED + "Back"));
        player.showFormWindow(profilframe);
    }
}
