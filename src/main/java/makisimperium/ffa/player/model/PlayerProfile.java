package makisimperium.ffa.player.model;

import java.util.UUID;

public final class PlayerProfile {

    private String uniqueId;
    private String lastKnownName;
    private PlayerStatistics statistics = new PlayerStatistics();
    private PlayerPreferences preferences = new PlayerPreferences();
    private String selectedKitId;

    public static PlayerProfile createDefault(UUID uniqueId, String playerName, boolean scoreboardEnabledByDefault) {
        PlayerProfile profile = new PlayerProfile();
        profile.uniqueId = uniqueId.toString();
        profile.lastKnownName = playerName;
        profile.preferences = new PlayerPreferences();
        profile.preferences.setScoreboardEnabled(scoreboardEnabledByDefault);
        profile.preferences.setLanguage(PlayerLanguage.ENGLISH);
        profile.statistics = new PlayerStatistics();
        return profile;
    }

    public UUID getUniqueId() {
        return UUID.fromString(uniqueId);
    }

    public String getUniqueIdRaw() {
        return uniqueId;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public PlayerStatistics getStatistics() {
        if (statistics == null) {
            statistics = new PlayerStatistics();
        }
        return statistics;
    }

    public PlayerPreferences getPreferences() {
        if (preferences == null) {
            preferences = new PlayerPreferences();
        }
        return preferences;
    }

    public String getSelectedKitId() {
        return selectedKitId;
    }

    public void setSelectedKitId(String selectedKitId) {
        this.selectedKitId = selectedKitId;
    }
}


