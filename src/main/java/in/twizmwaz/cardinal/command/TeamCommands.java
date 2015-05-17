package in.twizmwaz.cardinal.command;

import in.twizmwaz.cardinal.GameHandler;
import in.twizmwaz.cardinal.chat.ChatConstant;
import in.twizmwaz.cardinal.chat.LocalizedChatMessage;
import in.twizmwaz.cardinal.chat.UnlocalizedChatMessage;
import in.twizmwaz.cardinal.event.TeamNameChangeEvent;
import in.twizmwaz.cardinal.module.modules.team.TeamModule;
import in.twizmwaz.cardinal.util.ChatUtils;
import in.twizmwaz.cardinal.util.TeamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.NestedCommand;

public class TeamCommands {

    public static class TeamParentCommand {
        @Command(
                aliases = { "team" },
                desc = "Manage the teams in the match."
        )
        @NestedCommand({TeamCommands.class})
        public static void team(final CommandContext args, CommandSender sender) throws CommandException {
            
        }
    }

    @Command(aliases = {"force"}, desc = "Forces a player onto the team specified.", usage = "<player> <team>", min = 2)
    @CommandPermissions("cardinal.team.force")
    public static void force(final CommandContext cmd, CommandSender sender) throws CommandException {
        if (Bukkit.getPlayer(cmd.getString(1)) != null) {
            String msg = "";
            for (int i = 2; i < cmd.argsLength(); i++) {
                msg += cmd.getString(i) + " ";
            }
            msg = msg.trim();
            if (TeamUtils.getTeamByName(msg) != null) {
                TeamModule team = TeamUtils.getTeamByName(msg);
                if (!team.contains(Bukkit.getPlayer(cmd.getString(1)))) {
                    team.add(Bukkit.getPlayer(cmd.getString(1)), true, false);
                    sender.sendMessage(team.getColor() + Bukkit.getPlayer(cmd.getString(1)).getName() + ChatColor.GRAY + " forced to " + team.getCompleteName());
                } else throw new CommandException(new LocalizedChatMessage(ChatConstant.ERROR_ALREADY_ON_TEAM, TeamUtils.getTeamByPlayer(Bukkit.getPlayer(cmd.getString(1))).getColor() + Bukkit.getPlayer(cmd.getString(1)).getName() + ChatColor.RED, TeamUtils.getTeamByPlayer(Bukkit.getPlayer(cmd.getString(1))).getCompleteName()).getMessage(((Player) sender).getLocale()));
            } else {
                throw new CommandException(new LocalizedChatMessage(ChatConstant.ERROR_NO_TEAM_MATCH).getMessage(ChatUtils.getLocale(sender)));
            }
        } else {
            throw new CommandException(new LocalizedChatMessage(ChatConstant.ERROR_NO_PLAYER_MATCH).getMessage(ChatUtils.getLocale(sender)));
        }
    }

    @Command(aliases = {"alias"}, desc = "Renames a the team specified.", usage = "<team> <name>", min = 2)
    @CommandPermissions("cardinal.team.alias")
    public static void alias(final CommandContext cmd, CommandSender sender) throws CommandException {
        TeamModule team = TeamUtils.getTeamByName(cmd.getString(1));
        if (team != null) {
            String msg = "";
            for (int i = 2; i < cmd.argsLength(); i++) {
                msg += cmd.getString(i) + " ";
            }
            msg = msg.trim();
            String locale = ChatUtils.getLocale(sender);
            sender.sendMessage(ChatColor.GRAY + new LocalizedChatMessage(ChatConstant.GENERIC_TEAM_ALIAS, team.getCompleteName() + ChatColor.GRAY, team.getColor() + msg + ChatColor.GRAY).getMessage(locale));
            team.setName(msg);
            Bukkit.getServer().getPluginManager().callEvent(new TeamNameChangeEvent(team));
        } else {
            throw new CommandException(new LocalizedChatMessage(ChatConstant.ERROR_NO_TEAM_MATCH).getMessage(ChatUtils.getLocale(sender)));
        }
    }

    @Command(aliases = {"shuffle"}, desc = "Shuffles the teams.", usage = "", min = 0, max = 0)
    @CommandPermissions("cardinal.team.shuffle")
    public static void shuffle(final CommandContext cmd, CommandSender sender) throws CommandException {
        List<Player> playersToShuffle = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (TeamUtils.getTeamByPlayer(player) != null) {
                if (!TeamUtils.getTeamByPlayer(player).isObserver()) {
                    playersToShuffle.add(player);
                    TeamModule observers = TeamUtils.getTeamById("observers");
                    observers.add(player, true, false);
                }
            }
        }
        while (playersToShuffle.size() > 0) {
            Player player = playersToShuffle.get(new Random().nextInt(playersToShuffle.size()));
            TeamModule team = TeamUtils.getTeamWithFewestPlayers(GameHandler.getGameHandler().getMatch());
            team.add(player, true);
            playersToShuffle.remove(player);
        }
        String locale = ChatUtils.getLocale(sender);
        sender.sendMessage(ChatColor.GREEN + new LocalizedChatMessage(ChatConstant.GENERIC_TEAM_SHUFFLE).getMessage(locale));
    }

    @Command(aliases = {"size"}, desc = "Changes the specified team's size.", usage = "<team> <size>", min = 2)
    @CommandPermissions("cardinal.team.size")
    public static void size(final CommandContext cmd, CommandSender sender) throws CommandException {
        if (cmd.argsLength() >= 2) {
            TeamModule team = TeamUtils.getTeamByName(cmd.getString(1));
            if (team != null) {
                team.setMaxOverfill(Integer.parseInt(cmd.getString(2)));
                team.setMax(Integer.parseInt(cmd.getString(2)));
                sender.sendMessage(new LocalizedChatMessage(ChatConstant.GENERIC_TEAM_SIZE_CHANGED, TeamUtils.getTeamByName(cmd.getString(1)).getCompleteName() + ChatColor.WHITE, ChatColor.AQUA + cmd.getString(2)).getMessage(ChatUtils.getLocale(sender)));
            } else {
                throw new CommandException(new LocalizedChatMessage(ChatConstant.ERROR_NO_TEAM_MATCH).getMessage(ChatUtils.getLocale(sender)));
            }
        } else {
            throw new CommandUsageException("Too few arguments!", "/team size <team> <size>");
        }
    }

    @Command(aliases = {"myteam", "mt"}, desc = "Shows what team you are on", min = 0, max = 0)
    public static void myTeam(final CommandContext cmd, CommandSender sender) throws CommandException {
        if (!(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;
        player.sendMessage(new UnlocalizedChatMessage(ChatColor.GRAY + "{0}", new LocalizedChatMessage(ChatConstant.GENERIC_ON_TEAM, TeamUtils.getTeamByPlayer(player).getCompleteName())).getMessage(player.getLocale()));
    }

}
