package makisimperium.ffa.config;

public final class DatabaseConfiguration {

    private String host = "127.0.0.1";
    private int port = 3306;
    private String database = "ffa";
    private String username = "root";
    private String password = "change-me";
    private String table = "ffa_player_profiles";
    private boolean useSsl = false;
    private boolean allowPublicKeyRetrieval = true;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getTable() {
        return table;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public boolean isAllowPublicKeyRetrieval() {
        return allowPublicKeyRetrieval;
    }

    public boolean isConfigured() {
        if (host == null || host.isBlank()) {
            return false;
        }
        if (username == null || username.isBlank()) {
            return false;
        }
        if (password == null || password.isBlank() || "change-me".equalsIgnoreCase(password.trim())) {
            return false;
        }
        return database != null && !database.isBlank();
    }
}


