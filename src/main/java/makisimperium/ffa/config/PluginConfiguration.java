package makisimperium.ffa.config;

public final class PluginConfiguration {

    private String storageBackend = "MYSQL";
    private DatabaseConfiguration database = new DatabaseConfiguration();
    private ScoreboardConfiguration scoreboard = new ScoreboardConfiguration();

    public StorageBackend resolveStorageBackend() {
        return StorageBackend.fromText(storageBackend);
    }

    public DatabaseConfiguration getDatabase() {
        if (database == null) {
            database = new DatabaseConfiguration();
        }
        return database;
    }

    public ScoreboardConfiguration getScoreboard() {
        if (scoreboard == null) {
            scoreboard = new ScoreboardConfiguration();
        }
        return scoreboard;
    }

    public void normalize() {
        getDatabase();
        getScoreboard();
    }
}


