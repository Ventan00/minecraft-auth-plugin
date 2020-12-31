package me.ventan.venAuth;

import me.ventan.venAuth.events.Login;
import me.ventan.venAuth.utlis.FileManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main extends JavaPlugin {
    private static Main instance;
    private static FileManager manager;

    @Override
    public void onEnable(){
        instance =  this;
        manager = FileManager.getInstance();
        getServer().getPluginManager().registerEvents(new Login(),this);
    }
    public static Main getInstance(){
        return instance;
    }
    public static List<String> getProtectedNicks(){
        return manager.getProtectedNicks();
    }

    @Override
    public void onDisable(){
        manager.reread();
        manager = FileManager.getInstance();
    }
}
