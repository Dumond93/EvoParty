package net.ddns.evolutionary.evoparty.utility;


import me.blackvein.quests.Quests;
import net.ddns.evolutionary.evoparty.EvoParty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyUtils {

  Plugin evoParty = EvoParty.getInstance();

  // This section defines name spaced keys used throughout the utility class
    NamespacedKey activePartyKey = new NamespacedKey(evoParty, "ActivePartyKey");
    NamespacedKey partyMemberKey = new NamespacedKey(evoParty, "PartyMemberKey");
    NamespacedKey partyLeaderKey = new NamespacedKey(evoParty, "PartyLeaderKey");
    NamespacedKey partyChat = new NamespacedKey(evoParty, "PartyChat");
    NamespacedKey invitation = new NamespacedKey(evoParty, "Invitation");
    NamespacedKey scoreboard = new NamespacedKey(evoParty, "Scoreboard");
    NamespacedKey leaderChangedWorld = new NamespacedKey(evoParty, "worldChange");

    //Quests qp = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");

    public boolean isPlayerInParty(Player player) {
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        if (playerContainer.has(activePartyKey, PersistentDataType.STRING)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPartyLeader(Player player) {
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        if (playerContainer.has(partyLeaderKey, PersistentDataType.INTEGER)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPartyMember(Player player) {
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        if (playerContainer.has(partyMemberKey, PersistentDataType.INTEGER)) {
            return true;
        } else {
            return false;
        }
    }

    public Player getPartyLeader(List<Player> party) {
        Player player = null;
        for (Player p : party) {
            PersistentDataContainer pContainer = p.getPersistentDataContainer();
            if (pContainer.has(partyLeaderKey, PersistentDataType.INTEGER)) {
                player = p;
            }
        }
        return player;
    }

    public List<Player> getPartyMembers(List<Player> party) {
        List<Player> members = new ArrayList<>();
        for (Player p : party) {
            PersistentDataContainer pContainer = p.getPersistentDataContainer();
            if (pContainer.has(partyMemberKey, PersistentDataType.INTEGER)) {
                members.add(p);
            }
        }
        return members;
    }

    public List<Player> getParty(Player player) {
        List<Player> party = new ArrayList<>();
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        if (playerContainer.has(activePartyKey, PersistentDataType.STRING)) {
            String partyUUID = getPartyUUID(player);
            List<Player> onlinePlayers = (List<Player>) Bukkit.getServer().getOnlinePlayers();
            for (Player p : onlinePlayers) {
                PersistentDataContainer pContainer = p.getPersistentDataContainer();
                if (pContainer.has(activePartyKey, PersistentDataType.STRING)) {
                    String pUUID = pContainer.get(activePartyKey, PersistentDataType.STRING);
                    if (pUUID.equalsIgnoreCase(partyUUID)) {
                        party.add(p);
                    }
                }
            }
        }
        return party;
    }

    public String getPartyUUID(Player player) {
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        String uuid = playerContainer.get(activePartyKey, PersistentDataType.STRING);
        return uuid;
    }

    public List<Player> getPartyByUUID(String uuid){
        List<Player> party = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()){
            PersistentDataContainer pContainer = p.getPersistentDataContainer();
            if (pContainer.has(activePartyKey, PersistentDataType.STRING)){
                String worldPlayerUUID = pContainer.get(activePartyKey, PersistentDataType.STRING);
                if (uuid.equals(worldPlayerUUID)){
                    party = getParty(p);
                    break;
                }
            }
        }
        if (party.size() > 0){
            return party;
        } else {return null;}
    }

    public boolean isPartOfMyParty(Player player1, Player player2) {
        List<Player> party = getParty(player1);
        boolean isPartyOfMyParty = false;
        for (Player p : party) {
            if (p.getName().equalsIgnoreCase(player2.getName())) {
                isPartyOfMyParty = true;
            }
        }
        return isPartyOfMyParty;
    }
    public boolean checkPermissions(Player player, String permission){
        boolean result = true;
        if (evoParty.getConfig().getBoolean("PermissionBasedParties")) {
            if (player.hasPermission(permission)){
                result = true;
            } else {result = false;}
            if (player.hasPermission("evoparty.*")){
                result = true;
            }
        }
        return result;
    }

    public void startNewParty(Player player) {
        if (checkPermissions(player, "evoparty.start")) {
            if (!isPlayerInParty(player)) {
                PersistentDataContainer playerContainer = player.getPersistentDataContainer();
                playerContainer.set(activePartyKey, PersistentDataType.STRING, UUID.randomUUID().toString());
                playerContainer.set(partyLeaderKey, PersistentDataType.INTEGER, 1);
                PlayerJoinsParty playerJoinsParty = new PlayerJoinsParty(player);
                Bukkit.getPluginManager().callEvent(playerJoinsParty);
                player.sendMessage(ChatColor.GOLD + "You have started a new party");
            }
        } else {player.sendMessage(ChatColor.RED + "You do not have permission to use this command");}
    }

    public void leaveParty(Player player) {
        if (isPlayerInParty(player)) {
            if (isPartyLeader(player)) {
                List<Player> party = getParty(player);
                if (getPartyMembers(party) != null) {
                    List<Player> partyMembers = getPartyMembers(party);
                    for (Player p : partyMembers) {
                        PlayerLeavesParty playerLeavesParty = new PlayerLeavesParty(p, party);
                        Bukkit.getPluginManager().callEvent(playerLeavesParty);
                        removePersistentData(p);
                        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                        p.sendMessage(ChatColor.RED + "The leader has left the party, The party has been disbanded");
                    }
                }
                PlayerLeavesParty playerLeavesParty = new PlayerLeavesParty(player, party);
                Bukkit.getPluginManager().callEvent(playerLeavesParty);
                removePersistentData(player);
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                player.sendMessage(ChatColor.RED + "You have disbanded the party!");
            } else if (isPartyMember(player)) {
                List<Player> party = getParty(player);
                if (party.size() > 0) {
                    for (Player p : party) {
                        if (p.getName() != player.getName()) {
                            p.sendMessage(ChatColor.RED + player.getName() + " Has left the party");
                        }
                    }
                }
                PlayerLeavesParty playerLeavesParty = new PlayerLeavesParty(player, party);
                Bukkit.getPluginManager().callEvent(playerLeavesParty);
                Player partyLeader = getPartyLeader(party);
                removePersistentData(player);
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                player.sendMessage(ChatColor.RED + "You have left the party");
                partyBoard(getParty(partyLeader));

            }
        } else {
            player.sendMessage(ChatColor.GOLD + "You are not currently in a party");
        }
    }

    public void inviteToParty(Player inviter, Player invitee) {
        if (checkPermissions(inviter, "evoparty.invite")) {
            if (isPlayerInParty(inviter)) {
                PersistentDataContainer inviteeContainer = invitee.getPersistentDataContainer();
                String partyUUID = getPartyUUID(inviter);
                if (isPartyLeader(inviter)) {
                    if (getParty(inviter).size() < 4) {
                        if (!inviteeContainer.has(invitation, PersistentDataType.STRING)) {
                            inviteeContainer.set(invitation, PersistentDataType.STRING, inviter.getName());
                            inviteTimer invitetimer = new inviteTimer(invitee, inviter);
                            invitetimer.runTaskLater(evoParty, 1200);
                            invitee.sendMessage(ChatColor.GOLD + inviter.getName() + " has invited you to a party");
                            inviter.sendMessage(ChatColor.GOLD + "Invited " + invitee.getName() + " to the party");
                        } else {
                            inviter.sendMessage(ChatColor.RED + invitee.getName() + " already has a pending invitation");
                        }
                    } else {
                        inviter.sendMessage(ChatColor.RED + "Reached maximum party size of 4");
                    }
                } else {
                    inviter.sendMessage(ChatColor.RED + "Only party leaders can invite to a party");
                }
            }
        } else {inviter.sendMessage(ChatColor.RED + "You do not have permission to use this command");}
    }

    public boolean checkPartySize (Player player) {
        int maxPartySize = evoParty.getConfig().getInt("MaxPartySize");
        int partySize = getParty(player).size();
        if (partySize < maxPartySize) {
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Max party size of " + maxPartySize + " Reached!");
            return false;
        }
    }
    public void acceptPartyInvite(Player invitee) {
        PersistentDataContainer inviteeContainer = invitee.getPersistentDataContainer();
        if (inviteeContainer.has(invitation, PersistentDataType.STRING)) {
            Player inviter = Bukkit.getPlayer(inviteeContainer.get(invitation, PersistentDataType.STRING));
            String partyUUID = getPartyUUID(inviter);
            if (!inviteeContainer.has(activePartyKey, PersistentDataType.STRING)) {
                inviteeContainer.set(activePartyKey, PersistentDataType.STRING, partyUUID);
                inviteeContainer.set(partyMemberKey, PersistentDataType.INTEGER, 1);
                inviteeContainer.remove(invitation);
                PlayerJoinsParty playerJoinsParty = new PlayerJoinsParty(invitee);
                Bukkit.getPluginManager().callEvent(playerJoinsParty);
                if (getParty(inviter) != null) {
                    List<Player> party = getParty(inviter);
                    for (Player p : party) {
                        p.sendMessage(ChatColor.GREEN + invitee.getName() + " has joined the party");
                    }
                }
            } else {
                invitee.sendMessage(ChatColor.RED + "You are already in a party");
            }
        } else {
            invitee.sendMessage(ChatColor.RED + "You do not have any pending invitations");
        }
    }

    public void denyPartyInvite(Player invitee) {
        PersistentDataContainer inviteeContainer = invitee.getPersistentDataContainer();
        if (inviteeContainer.has(invitation, PersistentDataType.STRING)) {
            Player inviter = Bukkit.getPlayer(inviteeContainer.get(invitation, PersistentDataType.STRING));
            inviteeContainer.remove(invitation);
            inviter.sendMessage(ChatColor.RED + invitee.getName() + " has denied your party invite");
        } else invitee.sendMessage(ChatColor.RED + "You do not have any pending invitations");
    }

    public void kickFromParty(Player partyLeader, Player kickedPlayer) {
        if (isPartyLeader(partyLeader)) {
            if (isPartyMember(kickedPlayer)) {
                String leaderUUID = getPartyUUID(partyLeader);
                String memberUUID = getPartyUUID(kickedPlayer);
                if (leaderUUID.equals(memberUUID)) {
                    removePersistentData(kickedPlayer);
                    kickedPlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    kickedPlayer.sendMessage(ChatColor.RED + "You were kicked from the party");
                } else {
                    partyLeader.sendMessage(ChatColor.RED + kickedPlayer.getName() + " Is not in your party");
                }
            } else {
                partyLeader.sendMessage(ChatColor.RED + kickedPlayer.getName() + " Is not in your party");
            }
        } else {
            partyLeader.sendMessage(ChatColor.RED + "You must be the party leader to kick");
        }
    }

    public void teleportToPartyMember(Player teleporter, Player partyMember) {
        if (checkPermissions(teleporter, "evoparty.teleport")) {
            List<Player> party = getParty(teleporter);
            for (Player p : party) {
                if (p.getName().equalsIgnoreCase(partyMember.getName())) {
                    Location location = p.getLocation();
                    teleporter.teleport(location);
                    p.sendMessage(ChatColor.GREEN + teleporter.getName() + " has teleported to you");
                    teleporter.sendMessage(ChatColor.GREEN + "Teleporting to " + p.getName());
                }
            }
        } else {teleporter.sendMessage(ChatColor.RED + "You do not have permission to use this command");}
    }

    public void massPartyTeleport(Player teleporter) {
        List<Player> party = getParty(teleporter);
        Location location = teleporter.getLocation();
        if (checkPermissions(teleporter, "evoparty.teleportall")) {
            if (isPartyLeader(teleporter)) {
                for (Player p : party) {
                    p.teleport(location);
                    p.sendMessage(ChatColor.GOLD + teleporter.getName() + ChatColor.GREEN + " Has teleported the party to them");
                }
            } else {
                teleporter.sendMessage(ChatColor.RED + "Only party leaders may use tpall");
            }
        } else {teleporter.sendMessage(ChatColor.RED + "You do not have permission to use this command");}
    }

    public void togglePartyChat(Player player) {
        if (isPlayerInParty(player)) {
            if (checkPermissions(player, "evoparty.partychat")) {
                PersistentDataContainer playerContainer = player.getPersistentDataContainer();
                if (playerContainer.has(partyChat, PersistentDataType.INTEGER)) {
                    playerContainer.remove(partyChat);
                    player.sendMessage(ChatColor.DARK_AQUA + "Disabled Party Chat");
                } else {
                    playerContainer.set(partyChat, PersistentDataType.INTEGER, 1);
                    player.sendMessage(ChatColor.AQUA + "Enabled Party Chat");
                }
            } else {player.sendMessage(ChatColor.RED + "You do not have permission to use this command");}
        }
    }

    public void removePersistentData(Player player) {
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        playerContainer.remove(activePartyKey);
        playerContainer.remove(partyLeaderKey);
        playerContainer.remove(partyMemberKey);
        playerContainer.remove(partyChat);
        playerContainer.remove(invitation);
        playerContainer.remove(scoreboard);
    }

    public void partyBoard(List<Player> playerList) {
        ScoreboardManager sbManager = Bukkit.getScoreboardManager();
        Scoreboard board = sbManager.getNewScoreboard();
        Objective obj = board.registerNewObjective("EvoParty", "dummy", ChatColor.GREEN + "Active Party");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Team team = board.registerNewTeam(UUID.randomUUID().toString());
        team.setAllowFriendlyFire(friendlyFire());
        for (Player p : playerList) {
            PersistentDataContainer pContainer = p.getPersistentDataContainer();
            pContainer.set(scoreboard, PersistentDataType.INTEGER, 1);
            if (p.getHealth() == 0 | p.isDead()){
                Score score = obj.getScore(ChatColor.RED + p.getName());
                score.setScore((int) p.getHealth() * 5);
            } else if (p.getHealth() > 18) {
                Score score = obj.getScore(ChatColor.GOLD + p.getName());
                score.setScore(100);
            } else {
                Score score = obj.getScore(ChatColor.GOLD + p.getName());
                score.setScore((int) p.getHealth() * 5);
            }
            team.addEntry(p.getName());
            p.setScoreboard(board);
        }
    }
    public void partyBoardToggle(Player player) {
        if (evoParty.getConfig().getBoolean("AllowHudToggle")) {
            if (checkPermissions(player, "evoparty.hud")) {
                player.sendMessage(ChatColor.GOLD + "Turning off the HUD will cause friendly fire to be enabled!");
                PersistentDataContainer pContainer = player.getPersistentDataContainer();
                if (pContainer.has(scoreboard, PersistentDataType.INTEGER)) {
                    ScoreboardManager sbManager = Bukkit.getScoreboardManager();
                    Scoreboard board = sbManager.getNewScoreboard();
                    player.setScoreboard(board);
                    pContainer.remove(scoreboard);
                    player.sendMessage(ChatColor.AQUA + "Party Hud Off");
                } else {
                    pContainer.set(scoreboard, PersistentDataType.INTEGER, 1);
                    ScoreboardManager sbManager = Bukkit.getScoreboardManager();
                    Scoreboard board = sbManager.getNewScoreboard();
                    Objective obj = board.registerNewObjective("EvoParty", "dummy", ChatColor.GREEN + "Active Party");
                    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                    Team team = board.registerNewTeam(UUID.randomUUID().toString());
                    team.setAllowFriendlyFire(friendlyFire());
                    List<Player> playerList = getParty(player);
                    player.sendMessage(ChatColor.AQUA + "Party Hud On");
                    for (Player p : playerList) {
                        if (p.getHealth() == 0 | p.isDead()) {
                            Score score = obj.getScore(ChatColor.RED + p.getName());
                            score.setScore((int) p.getHealth() * 5);
                        } else if (p.getHealth() > 18) {
                            Score score = obj.getScore(ChatColor.GOLD + p.getName());
                            score.setScore(100);
                        } else {
                            Score score = obj.getScore(ChatColor.GOLD + p.getName());
                            score.setScore((int) p.getHealth() * 5);
                        }
                        team.addEntry(p.getName());
                        p.setScoreboard(board);
                    }
                }
            } else {player.sendMessage(ChatColor.RED + "You do not have permission to use this command");}
        } else {
            player.sendMessage(ChatColor.RED + "You may not toggle the hud");
        }
    }

    public void partyBoardUpdate(Player player){
        if (isPlayerInParty(player)) {
            List<Player> playerList = getParty(player);
            ScoreboardManager sbManager = Bukkit.getScoreboardManager();
            Scoreboard board = sbManager.getNewScoreboard();
            Objective obj = board.registerNewObjective("EvoParty", "dummy", ChatColor.GREEN + "Active Party");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            Team team = board.registerNewTeam(UUID.randomUUID().toString());
            team.setAllowFriendlyFire(friendlyFire());
            for (Player p : playerList) {
                PersistentDataContainer pContainer = p.getPersistentDataContainer();
                if (pContainer.has(scoreboard, PersistentDataType.INTEGER)) {
                    if (p.getHealth() == 0 | p.isDead()){
                        Score score = obj.getScore(ChatColor.STRIKETHROUGH +  p.getName());
                        score.setScore((int) p.getHealth());
                    } else if (p.getHealth() > 18) {
                        Score score = obj.getScore(ChatColor.GOLD + p.getName());
                        score.setScore(100);
                    } else {
                        Score score = obj.getScore(ChatColor.GOLD + p.getName());
                        score.setScore((int) p.getHealth() * 5);
                    }
                    team.addEntry(p.getName());
                    p.setScoreboard(board);
                }
            }
        }
    }

    public boolean friendlyFire(){
        if (evoParty.getConfig().getBoolean("FriendlyFire")){
            return true;
        } else {return false;}
    }

    public class inviteTimer extends BukkitRunnable {

        private Player invitee;
        private Player inviter;

        public inviteTimer(Player invitee, Player inviter) {
            this.invitee = invitee;
            this.inviter = inviter;
        }

        @Override
        public void run() {
            PersistentDataContainer container = invitee.getPersistentDataContainer();
            if (container.has(invitation, PersistentDataType.STRING)) {
                container.remove(invitation);
                invitee.sendMessage(ChatColor.RED + "You have failed to accept the invitation in time");
                inviter.sendMessage(ChatColor.GOLD + invitee.getName() + ChatColor.RED + " has denied your invitation");
            }
        }
    }

    public void leaderChangedWorld(Player player){
        PersistentDataContainer pContainer = player.getPersistentDataContainer();
        pContainer.set(leaderChangedWorld, PersistentDataType.INTEGER, 1);
        new BukkitRunnable(){
            @Override
            public void run() {
                pContainer.remove(leaderChangedWorld);
                player.sendMessage(ChatColor.RED + "You have chosen not to follow");
            }
        }.runTaskLater(evoParty, 200);
    }

////////////////////// This Section Defines Events for Use By Other Plugins /////////////////////////////////////////////////

    public class PlayerJoinsParty extends Event {

        private final Player player;
        private final HandlerList HANDLERS = new HandlerList();

        public PlayerJoinsParty(Player player) {
            this.player = player;
        }

        public Player getPlayer(){
            Player player = this.player;
            return player;
        }

        public List<Player> getEventParty() {
            Player player = this.player;
            return getParty(player);
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public class PlayerLeavesParty extends Event {

        private final Player player;
        private final List<Player> party;
        private final HandlerList HANDLERS = new HandlerList();

        public PlayerLeavesParty(Player player, List<Player> party) {
            this.player = player;
            this.party = party;
        }

        public Player getPlayer(){
            Player player = this.player;
            return player;
        }

        public List<Player> getEventParty() {
            List<Player> party = this.party;
            return party;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public HandlerList getHandlerList() {
            return HANDLERS;
        }
    }
}
