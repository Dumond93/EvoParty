package net.ddns.evolutionary.evoparty.events;

import me.blackvein.quests.Quests;
import net.ddns.evolutionary.EvoParty;
import net.ddns.evolutionary.utility.PartyUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Events implements Listener {

    Plugin evoParty = EvoParty.getInstance();
    PartyUtils partyUtils = new PartyUtils();
    Quests qp = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");


    @EventHandler
    public void onPlayerChats(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        String message = event.getMessage();
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        NamespacedKey partyChat = new NamespacedKey(evoParty, "PartyChat");
        if (playerContainer.has(partyChat, PersistentDataType.INTEGER)){
            for (Player p : partyUtils.getParty(player)){
                p.sendMessage(ChatColor.AQUA + "(PartyChat) [" + player.getName() + "] - " + message );
                Bukkit.getServer().getConsoleSender().sendMessage("(PartyChat) [" + player.getName() + "] - " + message );
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeaves(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        NamespacedKey activePartyKey = new NamespacedKey(evoParty, "ActivePartyKey");

        if (playerContainer.has(activePartyKey, PersistentDataType.STRING)) {
            partyUtils.leaveParty(player);
        }
    }

    @EventHandler
    public void playerDamageEvent(EntityDamageEvent event){

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            new BukkitRunnable() {
                @Override
                public void run() {
                    partyUtils.partyBoardUpdate(player);
                }
            }.runTaskLater(evoParty, 10);
        }
    }

    @EventHandler
    public void playerDeathEvent (PlayerDeathEvent event) {

        Player player = event.getEntity();
        if (partyUtils.isPlayerInParty(player)){
            List<Player> party = partyUtils.getParty(player);
            partyUtils.partyBoardUpdate(player);
            for (Player p : party) {
                p.sendMessage(ChatColor.AQUA + "Party member " + ChatColor.GOLD + player.getName() + ChatColor.AQUA + " has died");
            }
        }
    }

    @EventHandler
    public void playerGainsExp(PlayerExpChangeEvent event){
        Player player = event.getPlayer();
        int expAmount = event.getAmount();

        if (partyUtils.isPlayerInParty(player)) {
            if (expAmount > 0) {
                for (Player p : partyUtils.getParty(player)){
                    if (p != player) {
                        p.giveExp(expAmount);
                        p.sendMessage(ChatColor.AQUA + "Gained party experience" + ChatColor.GOLD + " + " + event.getAmount());
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerHealEvent(EntityRegainHealthEvent event) {

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            new BukkitRunnable() {
                @Override
                public void run() {
                    partyUtils.partyBoardUpdate(player);
                }
            }.runTaskLater(evoParty, 10);
        }
    }

    @EventHandler
    public void playerRespawnEvent (PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (partyUtils.isPlayerInParty(player)){
            new BukkitRunnable() {
                @Override
                public void run() {
                    partyUtils.partyBoardUpdate(player);
                }
            }.runTaskLater(evoParty, 20);
        }
    }
    /* These are quest related events to be implemented in the future for quest integration

    @EventHandler
    public void partyMemberCompletesQuest(QuesterPostCompleteQuestEvent event) {
        Quester quester = event.getQuester();
        Player player = quester.getPlayer();
        Quest quest = event.getQuest();

        if (evoParty.isPlayerInParty(player)) {
            for (Player p : evoParty.getParty(player)) {
                Quester pQuester = qp.getQuester(p.getUniqueId());
                if (!pQuester.getCompletedQuests().contains(quest)) {
                    quest.completeQuest(pQuester);
                    String questName = quest.getName();
                    p.sendMessage(ChatColor.AQUA + "Your party has completed the " + ChatColor.GOLD + questName + ChatColor.AQUA + " quest");
                }
            }
        }
    }

    @EventHandler
    public void playerTakesQuest(QuesterPreStartQuestEvent event) {
        Quester quester = event.getQuester();
        Player player = quester.getPlayer();
        Quest quest = event.getQuest();

        Quests qp = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");

        if (quest.getId() != "custom18") { // Ensure it does not run if the quest is quartzmine dungeon. Will implement config file for this later.
            if (evoParty.isPlayerInParty(player)) {
                for (Player p : evoParty.getParty(player)) {
                    Quester pQuester = qp.getQuester(p.getUniqueId());
                    if (p != player) {
                        if (!pQuester.getCurrentQuests().containsKey(quest)) {
                            syncQuest(pQuester, quest);
                            p.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.AQUA + " has started " + ChatColor.GOLD + quest.getName() + ChatColor.AQUA
                                    + " your progress is synced");
                        }
                    }
                }
            }
        }
    }

    public void syncQuest(Quester quester, Quest quest){
        new BukkitRunnable(){

            @Override
            public void run() {
                if (!quester.getCurrentQuests().containsKey(quest)) {
                    quester.takeQuest(quest, true);
                }
            }
        }.runTaskLater(evoParty, 40);
    }

    @EventHandler
    public void questProgressUpdated(QuesterPostUpdateObjectiveEvent event) {
        Quest quest = event.getQuest();
        Quester quester = event.getQuester();
        Player player = quester.getPlayer();

        if (evoParty.isPlayerInParty(player)) {
            for (Player p : evoParty.getParty(player)) {
                Quester pQuester = qp.getQuester(p.getUniqueId());
                if (pQuester.getCurrentQuests().containsKey(quest)) {
                    pQuester.hardDataPut(quest, quester.getQuestData(quest));
                    pQuester.updateJournal();
                }
            }
        }
    } */
}
