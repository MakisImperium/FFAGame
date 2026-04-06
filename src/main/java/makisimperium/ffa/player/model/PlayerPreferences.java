package makisimperium.ffa.player.model;

public final class PlayerPreferences {

    private boolean scoreboardEnabled = true;
    private String language = PlayerLanguage.ENGLISH.getCode();

    public boolean isScoreboardEnabled() {
        return scoreboardEnabled;
    }

    public void setScoreboardEnabled(boolean scoreboardEnabled) {
        this.scoreboardEnabled = scoreboardEnabled;
    }

    public PlayerLanguage getLanguage() {
        return PlayerLanguage.fromCode(language);
    }

    public String getLanguageCode() {
        return getLanguage().getCode();
    }

    public void setLanguage(PlayerLanguage language) {
        this.language = language == null ? PlayerLanguage.ENGLISH.getCode() : language.getCode();
    }
}


