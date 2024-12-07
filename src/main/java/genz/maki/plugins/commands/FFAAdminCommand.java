package genz.maki.plugins.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.form.MainForm;

import java.io.IOException;

public class FFAAdminCommand extends Command {

    private final MainForm form;
    private final Main plugin;

    public FFAAdminCommand(MainForm form, Main plugin) {
        super("ffaadmin", "Open FFA Admin Panel", "Usage: /ffaadmin", new String[]{"ffaadmin"});
        this.form = form;
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (player.hasPermission("ffa.admin")) {
                try {
                    form.sendAdminForm(player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                player.sendMessage(plugin.getPrefix() + TextFormat.RED + "You don't have permission to use this command!");
            }
        } else {
            commandSender.sendMessage(TextFormat.RED + "You can only use this in-game!");
        }
        return true;
    }
}
