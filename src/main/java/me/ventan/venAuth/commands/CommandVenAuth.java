package me.ventan.venAuth.commands;

import me.ventan.venAuth.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandVenAuth implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length!=1)
        {
            commandSender.sendMessage("Poprawne użycie: /venAuth <on|off>");
            return false;
        }else{
            if(args[0].compareTo("on")==0) {
                Main.setIsActive(true);
                commandSender.sendMessage("venAuth aktywny!");
            }
            else if(args[0].compareTo("off")==0) {
                Main.setIsActive(false);
                commandSender.sendMessage("venAuth wyłączony!");
            }
            else{
                commandSender.sendMessage("Poprawne użycie: /venAuth <on|off>");
                return false;
            }
            return true;
        }

    }
}
