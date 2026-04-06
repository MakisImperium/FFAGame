package makisimperium.ffa.messaging;

import cn.nukkit.utils.TextFormat;

public final class FfaMessageFormatter {

    private static final String PREFIX = TextFormat.DARK_GRAY + "[" + TextFormat.GOLD + "FFA" + TextFormat.GRAY + " Arena" + TextFormat.DARK_GRAY + "] "
            + TextFormat.RESET;

    private FfaMessageFormatter() {
    }

    public static String info(String text) {
        return PREFIX + TextFormat.AQUA + text;
    }

    public static String success(String text) {
        return PREFIX + TextFormat.GREEN + text;
    }

    public static String warning(String text) {
        return PREFIX + TextFormat.YELLOW + text;
    }

    public static String error(String text) {
        return PREFIX + TextFormat.RED + text;
    }
}


