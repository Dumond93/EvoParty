package net.ddns.evolutionary.evoparty.commands;

import net.ddns.evolutionary.evoparty.EvoParty;
import net.ddns.evolutionary.evoparty.utility.PartyUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class Party implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "You may only use this command in game!");
            return false;
        }

        Player player = (Player) commandSender;
        Plugin evoParty = EvoParty.getInstance();
        PartyUtils partyUtils = new PartyUtils();

        if (command.getName().equalsIgnoreCase("party")) {
            if (args.length == 0){
                player.sendMessage(ChatColor.YELLOW + "Valid options are [start] [leave] [invite] [hud] [chat] [lfg] [accept] [deny] [teleport]");
            }
            if (args.length == 1) {
                switch (args[0]) {
                    case "start":
                        partyUtils.startNewParty(player);
                        partyUtils.partyBoard(partyUtils.getParty(player));
                        break;
                    case "leave":
                        partyUtils.leaveParty(player);
                        break;
                    case "accept":
                        partyUtils.acceptPartyInvite(player);
                        partyUtils.partyBoard(partyUtils.getParty(player));
                        break;
                    case "deny":
                        partyUtils.denyPartyInvite(player);
                        break;
                    case "chat":
                        partyUtils.togglePartyChat(player);
                        break;
                    case "lfg":
                        partyUtils.toggleLFGChat(player);
                        break;
                    case "hud":
                        partyUtils.partyBoardToggle(player);
                        break;
                    case "tpall":
                        partyUtils.massPartyTeleport(player);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Valid options are [start] [leave] [invite] [hud] [chat] [lfg] [accept] [deny] [teleport]");
                }
            }
            if (args.length == 2) {
                switch (args[0]) {
                    case "invite":
                        try {
                            if (partyUtils.isPlayerInParty(player)) {
                                if (partyUtils.checkPartySize(player)) {
                                    Player invitee = Bukkit.getPlayer(args[1]);
                                    partyUtils.inviteToParty(player, invitee);
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You must start a party using /party start first");
                            }
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "No such player");
                        }
                        break;
                    case "kick":
                        try {
                            Player kickedPlayer = Bukkit.getPlayer(args[1]);
                            partyUtils.kickFromParty(player, kickedPlayer);
                            partyUtils.partyBoard(partyUtils.getParty(player));
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "No such player");
                        }
                        break;
                    case "tp":
                        try {
                            Player partyMember = Bukkit.getPlayer(args[1]);
                            if (partyUtils.isPartOfMyParty(player, partyMember)) {
                                partyUtils.teleportToPartyMember(player, partyMember);
                            } else {
                                player.sendMessage(ChatColor.RED + partyMember.getName() + " is not party of your party");
                            }
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "No such player");
                        }
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + "Valid options are [start] [leave] [invite] [accept] [deny] [tp]");
                }
            }
        }
        return true;
    }
}