package com.adamgaltrey.bvz.utils;

import com.adamgaltrey.bvz.BVZ;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

/**
 * Created by Adam on 24/08/2015.
 */
public class BungeeUtils {

    public static void registerChannels(BVZ plugin){
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    public static void redirectPlayer(Player p, String target) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(target);

        p.sendPluginMessage(BVZ.p, "BungeeCord", out.toByteArray());
    }

}
