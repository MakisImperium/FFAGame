package genz.maki.plugins.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import genz.maki.plugins.Main;
import genz.maki.plugins.form.MainForm;

import java.io.IOException;

public class FFACommand extends Command {

    private final MainForm form;

    public FFACommand(MainForm form) {
        super("ffa", "Öffne das FFA Menü", "Usage: /ffa", new String[]{"ffa"});
        this.form = form;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player) {
            try {
                form.sendMainframe((Player) commandSender);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            commandSender.sendMessage(TextFormat.RED + "You can only use this in-game!");
        }
        return true;
    }
}
