package me.ventan.venAuth.events;


import me.ventan.venAuth.Main;
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
        if(Main.getProtectedNicks().contains(event.getPlayer().getName())){
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
                        String ip = "77.55.209.66";
                        int port = 7584;
                        InetSocketAddress address = new InetSocketAddress(ip, port);
                        Socket socket = new Socket();
                        socket.connect(address);
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        JSONObject object = new JSONObject();
                        object.put("function", "checkUser");
                        object.put("ip", String.valueOf(ByteBuffer.wrap(event.getPlayer().getAddress().getAddress().getAddress()).getInt()));
                        object.put("nick", event.getPlayer().getName());
                        dos.writeUTF(object.toString());
                        String response = dis.readUTF();
                        authTimeout.interrupt();
                        Main.getInstance().getLogger().info("Got response from serwer: "+response);
                        socket.close();
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
