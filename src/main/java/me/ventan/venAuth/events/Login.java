package me.ventan.venAuth.events;


import me.ventan.venAuth.Main;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;

public class Login implements Listener {
    @EventHandler
    public void onLogin(PlayerJoinEvent event){
        if(Main.getProtectedNicks().contains(event.getPlayer().getDisplayName())){
                Thread runnable = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String ip = "146.59.3.145";
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
                            JSONTokener tokener = new JSONTokener(response);
                            JSONObject myresponse = new JSONObject(tokener);
                            if (!myresponse.getBoolean("success")) {
                                event.getPlayer().kickPlayer("Potwierdzenie wysłane na aplikację! Masz 15 minut na potwierdzenie osobowości");
                            } else {
                                event.getPlayer().sendMessage("Weryfikacja venAuth aktywna!");
                            }
                        } catch (IOException e) {
                            Main.getInstance().getLogger().log(Level.SEVERE,"Server connection exception in VenPlug");
                        }
                    }
                };
                runnable.run();
                Thread timeoutthread = new Thread(()->{
                    try {
                        Thread.sleep(5000);
                        Main.getInstance().getLogger().log(Level.SEVERE,"Serwer autoryzacji jest wyłączony!");
                        event.getPlayer().kickPlayer(ChatColor.RED +"Serwer autoryzacji jest wyłączony! Napisz do supportu na servernetpl@gmail.com");
                        runnable.interrupt();
                    } catch (InterruptedException e) {
                    }
                });
                timeoutthread.start();
        }
    }
}
