package com.adamgaltrey.bvz.data;

import com.adamgaltrey.bvz.BVZ;
import com.adamgaltrey.bvz.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerScoreboard {

	static Scoreboard s;
	static Objective o;

	static Map<String, Integer> points = new HashMap<String, Integer>();

	public static void init() {
		Bukkit.getScheduler().runTaskLater(BVZ.p, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				s = Bukkit.getScoreboardManager().getNewScoreboard();
				o = s.registerNewObjective("players", "dummy");
				o.setDisplaySlot(DisplaySlot.SIDEBAR);
				o.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChatColor.BOLD + "&a&lBvZ"));
                //todo: Add weather clear!
				//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/weather clear " + Integer.MAX_VALUE);
				o.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChatColor.BOLD + "&a&lBvZ " + ChatColor.RESET + "" + ChatColor.GOLD
						+ "00:" + (Config.startTimerSeconds > 9 ? Config.startTimerSeconds : "0" + Config.startTimerSeconds)));
			}
		}, 1);
	}

	/*public static int getMultipliedCoins(Player p, int c) {
		double mult = 1;
		if (p == null) {
			return c;
		}
		if (MongoUsersAPI.hasPermission(p.getName(), "coins.multiply.15")) {
			mult += 0.5;
		}
		if (MongoUsersAPI.hasPermission(p.getName(), "coins.multiply.2")) {
			mult += 1;
		}
		if (MongoUsersAPI.hasPermission(p.getName(), "coins.multiply.25")) {
			mult += 1.5;
		}
		if (MongoUsersAPI.hasPermission(p.getName(), "coins.multiply.3")) {
			mult += 2;
		}
		if (MongoUsersAPI.hasPermission(p.getName(), "coins.multiply.35")) {
			mult += 2.5;
		}
		return (int) mult * c;
	}*/
	
	public static void updateCountdown(int s){
		o.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChatColor.BOLD + "&a&lBvZ " + ChatColor.RESET + "" + ChatColor.GOLD
				+ "00:" + (s > 9 ? s : "0" + s)));
	}

	public static void updateSecondsElapsed(int s) {
		String mins = "00", seconds = "00";
		if (s > 60) {
			int m = (int) Math.floor((double) s / 60D);
			if (m > 9) {
				mins = Integer.toString(m);
			} else {
				mins = "0" + Integer.toString(m);
			}

			int rem = s - (m * 60);
			if (rem > 9) {
				seconds = Integer.toString(rem);
			} else {
				seconds = "0" + Integer.toString(rem);
			}
		} else {
			if (s > 9) {
				seconds = Integer.toString(s);
			} else {
				seconds = "0" + Integer.toString(s);
			}
		}

		o.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChatColor.BOLD + "&a&lBvZ " + ChatColor.RESET + "" + ChatColor.GOLD
				+ mins + ":" + seconds));
	}

	public static void addPlayer(Player p) {
		o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + p.getName())).setScore(0);
		p.setScoreboard(s);
		points.put(p.getName(), 0);
	}

	public static void removeScoreboard(Player p) {
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		s.resetScores(Bukkit.getOfflinePlayer(ChatColor.GREEN + p.getName()));
	}

	public static void addPoints(Player p, int c, boolean multiply) {
		int init = points.get(p.getName());
		points.put(p.getName(), c + init);
		setScore(p);
	}

	public static void deductPoints(Player p, int c) {
		int init = points.get(p.getName()) - c;
		points.put(p.getName(), init < 0 ? 0 : init);
		setScore(p);
	}

	private static void setScore(Player p) {
		o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + p.getName())).setScore(points.get(p.getName()));
	}

	public static int getPoints(Player p) {
		return points.get(p.getName());
	}

	public static String getWhoHasMostPoints() {
		String who = "";
		int most = -1;
		for (Entry<String, Integer> e : points.entrySet()) {
			if (e.getValue() > most) {
				most = e.getValue();
				who = e.getKey();
			}
		}
		return who;
	}

}
