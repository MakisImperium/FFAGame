package makisimperium.ffa.config;

public final class ScoreboardConfiguration {

    private boolean enabledByDefault = true;
    private int updateIntervalTicks = 20;

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public int getUpdateIntervalTicks() {
        return updateIntervalTicks;
    }
}


