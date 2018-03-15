package com.adamgaltrey.bvz.entities.turrets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adamgaltrey.bvz.Config;
import com.adamgaltrey.bvz.data.PlayerScoreboard;
import com.adamgaltrey.bvz.utils.Loc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TurretManager {

	public static Set<Turret> purchased = new HashSet<Turret>();

	public static Map<String, String> owner = new HashMap<String, String>();

	public static Set<String> purchasedRECORD = new HashSet<String>();

	// purchase turrets by right clicking them with a tool

	public static void turretClicked(Player p, Location turret) {
		if (turret.getBlock().getType().equals(Material.GOLD_BLOCK)) {
			// check purchase
			Loc l = new Loc(turret);
			if (purchasedRECORD.contains(l.toString())) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED
						+ "This turret has already been purchased."));
			} else {
				// check if can buy
				int cost = Config.turretCost;
				int points = PlayerScoreboard.getPoints(p);
				if (points >= cost) {
					// allow
					PlayerScoreboard.deductPoints(p, cost);
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.YELLOW + p.getName()
							+ ChatColor.GREEN + " has purchased a turret."));
					purchased.add(new Turret(turret));
					purchasedRECORD.add(l.toString());
					owner.put(l.toString(), p.getName());
				} else {
					// too poor
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED
							+ "You do not have enough points to purchase this."));
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&a&lBvZ&b] " + ChatColor.RED
							+ "This turret costs &a" + cost + " &cpoints."));
				}
			}
		} else {
			Loc l = new Loc(turret);
			if (purchasedRECORD.contains(l.toString())) {
				for (Turret t : purchased) {
					if (new Loc(t.l).toString().equals(l.toString())) {
						if (owner.containsKey(l.toString()) && owner.get(l.toString()).equalsIgnoreCase(p.getName())) {
							t.switchState();
						} else {
							p.sendMessage(ChatColor.RED + "Only the owner can change this turret's state.");
						}
						break;
					}
				}
			}
		}
	}

}
