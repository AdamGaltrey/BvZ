package com.adamgaltrey.bvz.game;

import com.adamgaltrey.bvz.fx.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.data.PlayerScoreboard;
//import com.adamki11s.minigameinstance.//MinigameAPI;

public class Countdown implements Runnable {

    private int c = Config.startTimerSeconds;
    private final int start;

    private BukkitTask task;

    public int getTimeLeft() {
        return c;
    }

    Title former;

    private boolean force;

    public Countdown(int countFrom, boolean force) {
        this.force = force;
        this.c = countFrom;
        this.start = countFrom;
        //Bukkit.broadcastMessage(ChatColor.GREEN + "The game will begin in " + countFrom + " seconds!");
        former = new Title("", "15");
        former.setTitleColor(ChatColor.WHITE);
        former.setSubtitleColor(ChatColor.GREEN);
        former.broadcast();
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public void cancelCountdown(boolean msg) {
        Game.countdownStarted = false;
        if (msg) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] &aCountdown cancelled&a!"));
        }
        c = start;
        task.cancel();
    }

    public void setCount(int c) {
        this.c = c;
    }

    @Override
    public void run() {
        if (c > -1) {
            if (!force && Bukkit.getOnlinePlayers().size() < Config.getMaxPlayers()) {
                //we do not have a full game
                BVZ.game.resetCountdown();
                task.cancel();
                PlayerScoreboard.updateCountdown(Config.getStartTimerSeconds());
                return;
            }
            if (c == 0) {
                //TODO START GAME
                //MinigameAPI.startGame();
                //Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] &aThe game has begun&a!"));
                former = new Title("", "The game has begun!", 1, 1, 2);
                former.setTitleColor(ChatColor.WHITE);
                former.setSubtitleColor(ChatColor.GREEN);
                former.broadcast();
                BVZ.game.startGame();
                task.cancel();
            } else if (c > 0) {
                former = new Title("", c + "");
                former.setSubtitleColor(ChatColor.GREEN);
                former.broadcast();
            }

            /*else if (c == 1) {
                //Bukkit.broadcastMessage(ChatColor
                //        .translateAlternateColorCodes('&', "&b[&a&lBvZ&b] &aThe game will begin in &c1 &csecond&a!"));
                former = new Title("", c + "");
                former.setSubtitleColor(ChatColor.GREEN);
                former.broadcast();
            } else if (c <= 5 || c == 10 || c == 15 || c == 30 || c == 45) {
                //Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] &aThe game will begin in &c" + c
                //         + " &cseconds&a!"));
                former = new Title("", c + "");
                former.setSubtitleColor(ChatColor.GREEN);
                former.broadcast();
            }*/
            PlayerScoreboard.updateCountdown(c);
            c--;
        }
    }

}
