package makisimperium.ffa.player.model;

public final class PlayerStatistics {

    private int kills;
    private int deaths;
    private int currentKillstreak;
    private int bestKillstreak;

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getCurrentKillstreak() {
        return currentKillstreak;
    }

    public int getBestKillstreak() {
        return bestKillstreak;
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
    }

    public void setDeaths(int deaths) {
        this.deaths = Math.max(0, deaths);
    }

    public void setCurrentKillstreak(int currentKillstreak) {
        this.currentKillstreak = Math.max(0, currentKillstreak);
    }

    public void setBestKillstreak(int bestKillstreak) {
        this.bestKillstreak = Math.max(0, bestKillstreak);
        if (this.bestKillstreak < this.currentKillstreak) {
            this.bestKillstreak = this.currentKillstreak;
        }
    }

    public void registerKill() {
        kills++;
        currentKillstreak++;
        if (currentKillstreak > bestKillstreak) {
            bestKillstreak = currentKillstreak;
        }
    }

    public void registerDeath() {
        deaths++;
        currentKillstreak = 0;
    }
}


