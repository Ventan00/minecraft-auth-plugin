package me.ventan.venAuth.events;


import me.ventan.venAuth.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Login implements Listener {
    @EventHandler
    public void onLogin(PlayerJoinEvent event){
        if(Main.getProtectedNicks().contains(event.getPlayer().getDisplayName())){
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            String ip = "127.0.0.1";
                            int port = 7584;
                            InetSocketAddress address = new InetSocketAddress(ip, port);
                            Socket socket = new Socket();
                            socket.connect(address);
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            DataInputStream dis = new DataInputStream(socket.getInputStream());
                            JSONObject object = new JSONObject();
                            object.put("function", "checkUser");
                            JSONObject data = new JSONObject();
                            data.put("ip", ByteBuffer.wrap(event.getPlayer().getAddress().getAddress().getAddress()).getInt());
                            data.put("nick", event.getPlayer().getDisplayName());
                            object.put("data", data);
                            dos.writeUTF(object.toString());
                            String response = dis.readUTF();
                            socket.close();
                            JSONParser jsonParser = new JSONParser();
                            JSONObject myresponse = (JSONObject) jsonParser.parse(response);
                            if (!object.getBoolean("success")) {
                                event.getPlayer().kickPlayer("Potwierdzenie wysłane na aplikację! Masz 15 minut na potwierdzenie osobowości");
                            } else {
                                event.getPlayer().sendMessage("Weryfikacja venAuth aktywna!");
                            }
                        } catch (IOException | ParseException e) {
                            System.err.println("Server connection exception in VenPlug");
                        }
                    }
                };
                runnable.run();
        }
    }
}
