package me.ventan.utlis;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FileManager {
    public static FileManager instance = new FileManager();

    //server params
    private String ip;
    private int port;
    private String password;

    //database params
    private String db_name;
    private String db_username;
    private String db_password;
    private String db_server;
    private int db_port;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getDb_name() {
        return db_name;
    }

    public String getDb_username() {
        return db_username;
    }

    public String getDb_password() {
        return db_password;
    }

    public String getDb_server() {
        return db_server;
    }

    public int getDb_port() {
        return db_port;
    }

    private FileManager(){
        try {
            File configFile = new File("config.yml");
            if(!configFile.exists()){
                configFile.createNewFile();

                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream("data.yml");

                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                FileWriter writer = new FileWriter(configFile);
                writer.write(content);
                writer.close();
                Logger.getInstance().addLogs("Pierwsze włączenie! Został stworzony plik konfiguracyjny. Uzupełnij go i włącz serwer ponownie.");
                System.exit(0);

            }
            FileReader reader = new FileReader(configFile);
            Yaml yaml = new Yaml();
            Map<String, Object> object = yaml.load(reader);

            ip = (String) object.get("server-adress");
            port = Integer.parseInt((String) object.get("server-port"));
            password = (String) object.get("server-password");
            db_name = (String) object.get("mysql-server-database");
            db_username = (String) object.get("mysql-server-user");
            db_password = (String) object.get("mysql-server-password");
            db_server = (String) object.get("mysql-server-adress");
            db_port = Integer.parseInt((String) object.get("mysql-server-port")); ;

        } catch (IOException e) {
            Logger.getInstance().addLogs(e);
        }
    }
    public static FileManager getInstance(){return instance;}

}