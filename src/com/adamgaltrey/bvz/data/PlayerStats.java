package com.adamgaltrey.bvz.data;

/**
 * Created by Adam on 24/08/2015.
 */
public class PlayerStats {

    private long gamesPlayed, gamesWon, wavesCleared, zombieKills, maxWave;

    public PlayerStats(long gamesPlayed, long gamesWon, long wavesCleared, long zombieKills, long maxWave) {
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.wavesCleared = wavesCleared;
        this.zombieKills = zombieKills;
        this.maxWave = maxWave;
    }

    public long getGamesPlayed() {
        return gamesPlayed;
    }

    public long getGamesWon() {
        return gamesWon;
    }

    public long getWavesCleared() {
        return wavesCleared;
    }

    public long getZombieKills() {
        return zombieKills;
    }

    public long getMaxWave() {
        return maxWave;
    }

    /*
        Games played and won and max wave will only update at the end of the session
     */

    public void waveCleared(int wave){
        wavesCleared++;

        if (wave > maxWave) {
            maxWave = wave;
        }
    }

    public void zombieKilled(){
        zombieKills++;
    }

    public void addZombieKills(int kills) {
        zombieKills += kills;
    }


}
