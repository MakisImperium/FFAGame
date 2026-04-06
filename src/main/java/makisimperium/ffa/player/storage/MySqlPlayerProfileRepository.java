package makisimperium.ffa.player.storage;

import makisimperium.ffa.config.DatabaseConfiguration;
import makisimperium.ffa.player.model.PlayerLanguage;
import makisimperium.ffa.player.model.PlayerPreferences;
import makisimperium.ffa.player.model.PlayerProfile;
import makisimperium.ffa.player.model.PlayerStatistics;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MySqlPlayerProfileRepository implements PlayerProfileRepository {

    private final DatabaseConfiguration databaseConfiguration;
    private final String databaseName;
    private final String tableName;

    public MySqlPlayerProfileRepository(DatabaseConfiguration databaseConfiguration) throws Exception {
        this.databaseConfiguration = databaseConfiguration;
        this.databaseName = sanitizeDatabaseName(databaseConfiguration.getDatabase());
        this.tableName = sanitizeTableName(databaseConfiguration.getTable());
        Class.forName("com.mysql.cj.jdbc.Driver");
        initializeSchema();
    }

    @Override
    public Optional<PlayerProfile> findByUniqueId(UUID uniqueId) throws Exception {
        String sql = "SELECT uuid, username, kills, deaths, current_killstreak, best_killstreak, " +
                "scoreboard_enabled, language, selected_kit FROM `" + tableName + "` WHERE uuid = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uniqueId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapProfile(resultSet));
            }
        }
    }

    @Override
    public List<PlayerProfile> loadAllProfiles() throws Exception {
        String sql = "SELECT uuid, username, kills, deaths, current_killstreak, best_killstreak, " +
                "scoreboard_enabled, language, selected_kit FROM `" + tableName + "`";
        List<PlayerProfile> profiles = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                profiles.add(mapProfile(resultSet));
            }
        }
        return profiles;
    }

    @Override
    public void save(PlayerProfile profile) throws Exception {
        String sql = "INSERT INTO `" + tableName + "` " +
                "(uuid, username, kills, deaths, current_killstreak, best_killstreak, scoreboard_enabled, language, selected_kit) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE username = VALUES(username), kills = VALUES(kills), deaths = VALUES(deaths), " +
                "current_killstreak = VALUES(current_killstreak), best_killstreak = VALUES(best_killstreak), " +
                "scoreboard_enabled = VALUES(scoreboard_enabled), language = VALUES(language), selected_kit = VALUES(selected_kit)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            PlayerStatistics statistics = profile.getStatistics();
            PlayerPreferences preferences = profile.getPreferences();

            statement.setString(1, profile.getUniqueIdRaw());
            statement.setString(2, profile.getLastKnownName());
            statement.setInt(3, statistics.getKills());
            statement.setInt(4, statistics.getDeaths());
            statement.setInt(5, statistics.getCurrentKillstreak());
            statement.setInt(6, statistics.getBestKillstreak());
            statement.setBoolean(7, preferences.isScoreboardEnabled());
            statement.setString(8, preferences.getLanguageCode());
            if (profile.getSelectedKitId() == null || profile.getSelectedKitId().isBlank()) {
                statement.setNull(9, Types.VARCHAR);
            } else {
                statement.setString(9, profile.getSelectedKitId());
            }
            statement.executeUpdate();
        }
    }

    @Override
    public boolean ping() throws Exception {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet ignored = statement.executeQuery()) {
            return true;
        }
    }

    private PlayerProfile mapProfile(ResultSet resultSet) throws SQLException {
        PlayerProfile profile = PlayerProfile.createDefault(
                UUID.fromString(resultSet.getString("uuid")),
                resultSet.getString("username"),
                resultSet.getBoolean("scoreboard_enabled")
        );

        PlayerStatistics statistics = profile.getStatistics();
        statistics.setKills(resultSet.getInt("kills"));
        statistics.setDeaths(resultSet.getInt("deaths"));
        statistics.setCurrentKillstreak(resultSet.getInt("current_killstreak"));
        statistics.setBestKillstreak(resultSet.getInt("best_killstreak"));

        profile.setSelectedKitId(resultSet.getString("selected_kit"));
        profile.setLastKnownName(resultSet.getString("username"));
        profile.getPreferences().setScoreboardEnabled(resultSet.getBoolean("scoreboard_enabled"));
        profile.getPreferences().setLanguage(PlayerLanguage.fromCode(resultSet.getString("language")));
        return profile;
    }

    private void initializeSchema() throws Exception {
        ensureDatabaseExists();
        String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "username VARCHAR(32) NOT NULL," +
                "kills INT NOT NULL DEFAULT 0," +
                "deaths INT NOT NULL DEFAULT 0," +
                "current_killstreak INT NOT NULL DEFAULT 0," +
                "best_killstreak INT NOT NULL DEFAULT 0," +
                "scoreboard_enabled BOOLEAN NOT NULL DEFAULT TRUE," +
                "language VARCHAR(8) NOT NULL DEFAULT 'EN'," +
                "selected_kit VARCHAR(64) NULL" +
                ")";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
        ensureLanguageColumnExists();
    }

    private void ensureLanguageColumnExists() throws SQLException {
        try (Connection connection = openConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, tableName, "language")) {
                if (columns.next()) {
                    return;
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "ALTER TABLE `" + tableName + "` ADD COLUMN language VARCHAR(8) NOT NULL DEFAULT 'EN'")) {
                statement.execute();
            }
        }
    }

    private void ensureDatabaseExists() throws SQLException {
        String sql = "CREATE DATABASE IF NOT EXISTS `" + databaseName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
        try (Connection connection = openServerConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private Connection openConnection() throws SQLException {
        String url = "jdbc:mysql://" + databaseConfiguration.getHost() + ":" + databaseConfiguration.getPort() + "/" +
                databaseName +
                "?createDatabaseIfNotExist=true" +
                "&useUnicode=true" +
                "&characterEncoding=utf8" +
                "&useSSL=" + databaseConfiguration.isUseSsl() +
                "&allowPublicKeyRetrieval=" + databaseConfiguration.isAllowPublicKeyRetrieval() +
                "&serverTimezone=UTC";
        return DriverManager.getConnection(url, databaseConfiguration.getUsername(), databaseConfiguration.getPassword());
    }

    private Connection openServerConnection() throws SQLException {
        String url = "jdbc:mysql://" + databaseConfiguration.getHost() + ":" + databaseConfiguration.getPort() + "/" +
                "?useUnicode=true" +
                "&characterEncoding=utf8" +
                "&useSSL=" + databaseConfiguration.isUseSsl() +
                "&allowPublicKeyRetrieval=" + databaseConfiguration.isAllowPublicKeyRetrieval() +
                "&serverTimezone=UTC";
        return DriverManager.getConnection(url, databaseConfiguration.getUsername(), databaseConfiguration.getPassword());
    }

    private String sanitizeDatabaseName(String rawDatabaseName) {
        if (rawDatabaseName == null || rawDatabaseName.isBlank()) {
            return "ffa";
        }
        String sanitized = rawDatabaseName.replaceAll("[^a-zA-Z0-9_]", "");
        return sanitized.isBlank() ? "ffa" : sanitized;
    }

    private String sanitizeTableName(String rawTableName) {
        if (rawTableName == null || rawTableName.isBlank()) {
            return "ffa_player_profiles";
        }
        String sanitized = rawTableName.replaceAll("[^a-zA-Z0-9_]", "");
        return sanitized.isBlank() ? "ffa_player_profiles" : sanitized;
    }
}


