package com.uddernetworks.space.command;

import com.uddernetworks.command.Argument;
import com.uddernetworks.command.ArgumentError;
import com.uddernetworks.command.ArgumentList;
import com.uddernetworks.command.Command;
import com.uddernetworks.space.generator.MoonGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(name = "space", aliases = {"sp"}, consoleAllow = false, minArgs = 1, maxArgs = 2)
public class SpaceCommand {

    @Argument(format = "generate *")
    public void start(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;

        WorldCreator creator = new WorldCreator(args.nextArg().getString());
        creator.environment(World.Environment.NORMAL);
        creator.generator(new MoonGenerator());
        World moon = Bukkit.getServer().createWorld(creator);

        player.teleport(moon.getSpawnLocation());
    }

    @ArgumentError
    public void argumentError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + "Error while executing command: " + message);
    }

}
