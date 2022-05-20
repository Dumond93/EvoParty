package net.ddns.evolutionary.evoparty;


import net.ddns.evolutionary.evoparty.commands.Party;
import net.ddns.evolutionary.evoparty.events.Events;
import net.ddns.evolutionary.evoparty.utility.Metrics;
import net.ddns.evolutionary.evoparty.utility.PartyUtils;
import net.ddns.evolutionary.evoparty.utility.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EvoParty extends JavaPlugin {


    private static PartyUtils partyUtils;
    private static EvoParty instance;


    public static EvoParty getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig().addDefaults(this.getConfig().getDefaults());
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        instance = this;
        int pluginId = 14452;
        new UpdateChecker(this, 100237).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("EvoParty is up to date.");
            } else {
                getLogger().info(ChatColor.AQUA + "There is a new update available for EvoParty. Please download from spigot.");
            }
        });
        Metrics metrics = new Metrics(this, pluginId);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "EvoParty Plugin Enabled!");
        getServer().getPluginManager().registerEvents(new Events(), this);
        getCommand("party").setExecutor(new Party());
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "EvoParty Plugin Disabled!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            PartyUtils partyUtils = new PartyUtils();
            partyUtils.removePersistentData(p);
        }
    }

    public static PartyUtils getPartyUtils() {
        return partyUtils;
    }
}




