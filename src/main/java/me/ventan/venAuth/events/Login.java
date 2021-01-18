package me.ventan.venAuth.events;


import com.google.gson.JsonObject;
import me.ventan.venAuth.Main;
import me.ventan.venAuth.utlis.FileManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;

public class Login implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerJoinEvent event){
        if(Main.getProtectedNicks().contains(event.getPlayer().getName()) && Main.getIsActive()){
            Main.getInstance().getLogger().info(ChatColor.RED+" OP player connected, sending verification request");
            Thread thread = new Thread() {
                @Override
                public void run(){
                    Thread me = this;
                    Thread authTimeout = new Thread(){
                        @Override
                        public void run(){
                            try {
                                Thread.sleep(10000);
                                Main.getInstance().getLogger().log(Level.SEVERE,"Serwer autoryzacji jest wyłączony!");
                                Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new Runnable() {
                                    @Override
                                    public void run() {
                                        event.getPlayer().kickPlayer(ChatColor.RED +"Serwer autoryzacji jest wyłączony! Napisz do supportu na servernetpl@gmail.com");
                                    }
                                });
                                me.interrupt();
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                    authTimeout.start();
                    try {
                        String ip = FileManager.getInstance().getIp();
                        int port = FileManager.getInstance().getPort();
                        InetSocketAddress address = new InetSocketAddress(ip, port);
                        Socket socket = new Socket();
                        socket.connect(address);
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        JSONObject object = new JSONObject();
                        object.put("type","request");
                        object.put("requestFor","check");
                        JSONObject data = new JSONObject();
                        data.put("ip",String.valueOf(ByteBuffer.wrap(event.getPlayer().getAddress().getAddress().getAddress()).getInt()));
                        data.put("nick", event.getPlayer().getName());
                        object.put("data",data);
                        dos.writeUTF(object.toString());
                        dos.flush();
                        String response = dis.readUTF();
                        authTimeout.interrupt();
                        Main.getInstance().getLogger().info("Got response from serwer: "+response);
                        JSONTokener tokener = new JSONTokener(response);
                        JSONObject myresponse = new JSONObject(tokener);
                        if (!myresponse.getBoolean("success")) {
                            Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    event.getPlayer().kickPlayer("Potwierdzenie wysłane na aplikację! Masz 15 minut na potwierdzenie osobowości");
                                }
                            });
                        } else {
                            Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new Runnable() {
                                @Override
                                public void run() {
                                    event.getPlayer().sendMessage(ChatColor.AQUA+"Weryfikacja venAuth aktywna!");
                                }
                            });
                        }
                    } catch (IOException e) {
                        Main.getInstance().getLogger().log(Level.SEVERE,"Server connection exception in VenPlug");
                    }


                }
            };
            thread.start();
        }
    }
}
