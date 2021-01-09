package me.ventan.venAuth.events;


import me.ventan.venAuth.Main;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;

public class Login implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerJoinEvent event){
        if(Main.getProtectedNicks().contains(event.getPlayer().getName())){
            System.out.println("first call");
            Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new BukkitRunnable() {
                @Override
                public void run() {
                    BukkitRunnable task = this;
                    BukkitTask task1 = Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(),()->{
                        try {
                            Thread.sleep(10000);
                            Main.getInstance().getLogger().log(Level.SEVERE,"Serwer autoryzacji jest wyłączony!");
                            event.getPlayer().kickPlayer(ChatColor.RED +"Serwer autoryzacji jest wyłączony! Napisz do supportu na servernetpl@gmail.com");
                            Main.getInstance().getServer().getScheduler().cancelTask(task.getTaskId());
                            System.out.println("canceling");
                        } catch (InterruptedException e) {
                        }
                    });
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
                        task1.cancel();
                        System.out.println("got response");
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
            });
        }
    }
}
