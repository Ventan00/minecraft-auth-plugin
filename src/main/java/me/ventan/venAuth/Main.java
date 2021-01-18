package me.ventan.venAuth;

import me.ventan.venAuth.commands.CommandVenAuth;
import me.ventan.venAuth.events.Login;
import me.ventan.venAuth.utlis.BanConnector;
import me.ventan.venAuth.utlis.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main extends JavaPlugin {
    private static Main instance;
    private static FileManager manager;
    private static BanConnector banConnector;
    private static boolean isActive = true;
    @Override
    public void onEnable(){
        instance =  this;
        manager = FileManager.getInstance();
        banConnector = BanConnector.getInstance();


        getServer().getPluginManager().registerEvents(new Login(),this);
        this.getCommand("venAuth").setExecutor(new CommandVenAuth());
    }
    public static Main getInstance(){
        return instance;
    }
    public static List<String> getProtectedNicks(){
        return manager.getProtectedNicks();
    }
    public static boolean getIsActive(){return isActive;}
    public static void setIsActive(boolean value){isActive=value;}

    @Override
    public void onDisable(){
        manager.reread();
        manager = FileManager.getInstance();
    }
}
