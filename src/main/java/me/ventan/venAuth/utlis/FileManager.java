package me.ventan.venAuth.utlis;

import me.ventan.venAuth.Main;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static List<String> protectedNicks =  new ArrayList<>();
    private static String ip ;
    private static int port ;
    private static String password ;
    private static FileManager instance =  new FileManager();

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    private FileManager(){
        try (FileReader reader = new FileReader("ops.json"))
        {
            JSONTokener tokener = new JSONTokener(reader);
            JSONArray ops = new JSONArray(tokener);
            ops.forEach( op -> protectedNicks.add(((JSONObject)op).getString("name")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File config = new File(Main.getInstance().getDataFolder(), "config.yml");
        FileConfiguration configuration = Main.getInstance().getConfig();
        if(!config.exists()){
            configuration.addDefault("auth-server-ip","127.0.0.1");
            configuration.addDefault("auth-server-port","7584");
            configuration.addDefault("auth-server-password","password");
            try {
                configuration.save(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ip="127.0.0.1";
            port=7584;
            password="password";
            try {
                configuration.save(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                configuration.load(config);
                ip= configuration.get("auth-server-ip").toString();
                password= configuration.get("auth-server-password").toString();
                port= Integer.valueOf(configuration.get("auth-server-port").toString());
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
                ip="127.0.0.1";
                port=7584;
                password="password";
            }
        }

    }
    public static FileManager getInstance(){return instance;}
    public List<String> getProtectedNicks(){return protectedNicks;}
    public FileManager reread() {return new FileManager();}
}
