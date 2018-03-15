package com.adamgaltrey.bvz.entities.barriers;

import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.data.PlayerScoreboard;
import com.adamgaltrey.bvz.utils.Loc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class BarrierManager {

	public static Set<String> purchased = new HashSet<String>();

	// 0=purchase loc
	public static void passBarrierPurchase(Player p, Loc l) {
		for (Entry<String, Loc[]> e : Config.getBarriers().entrySet()) {
			if (e.getValue()[0].toString().equals(l.toString())) {
				// loc matches
				if (purchased.contains(e.getKey())) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED
							+ "This barrier has already been purchased."));
				} else {
					// try and buy it
					int cost = Config.barrierCost.get(e.getKey());
					int points = PlayerScoreboard.getPoints(p);
					if (points >= cost) {

						// allow
						PlayerScoreboard.deductPoints(p, cost);
						BarrierPreBuilder.buildActual(e.getKey());
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
								"&b[&a&lBvZ&b] " + ChatColor.YELLOW + p.getName() + ChatColor.GREEN + " has purchased a barrier."));
						purchased.add(e.getKey());
					} else {
						// too poor
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED
								+ "You do not have enough points to purchase this."));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED
								+ "This barrier costs &a" + cost + " &cpoints."));
					}
				}
				break;
			}
		}
	}

}
