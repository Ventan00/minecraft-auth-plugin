package me.ventan.venAuth.utlis;

import me.ventan.venAuth.Main;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

public class BanConnector extends Thread {
    private static BanConnector instance = new BanConnector();

    String ip;
    int port;
    String password;
    private Socket socket;
    DataInputStream dis;

    private BanConnector() {
        ip = FileManager.getInstance().getIp();
        port = FileManager.getInstance().getPort();
        password = FileManager.getInstance().getPassword();
        this.start();
    }
    public static BanConnector getInstance(){return instance;}
    @Override
    public void run(){
        try {
            InetSocketAddress address = new InetSocketAddress(ip, port);
            System.out.println(address);
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.connect(address);
            dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            JSONObject handshake =  new JSONObject();
            handshake.put("type","handshake");
            handshake.put("device","server");
            JSONObject data1 = new JSONObject();
            data1.put("password",password);
            handshake.put("data",data1);
            dos.writeUTF(handshake.toString());
            JSONObject temp = new JSONObject(dis.readUTF());
            if(!temp.getBoolean("success")) {
                interrupt();
                return;
            }
            Thread t = new Thread(()->{
                try {
                    JSONObject ping = new JSONObject();
                    ping.put("type",ping);
                    while (!Thread.currentThread().isInterrupted()){
                        Thread.sleep(600000);
                        dos.writeUTF(ping.toString());
                        dos.flush();
                    }
                } catch (InterruptedException | IOException e) {
                }
            });
            t.start();
            while (!Thread.currentThread().isInterrupted()){
                JSONObject object = new JSONObject(dis.readUTF());
                if(object.getString("type").compareTo("add_ban")==0){
                    JSONObject data = object.getJSONObject("data");
                    if(data.getString("type").compareTo("temp")==0) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.HOUR_OF_DAY, 24);
                        Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                Bukkit.getBanList(BanList.Type.IP).addBan(getIP(data.getInt("ip")), "Nieudana weryfikacja venAuth", calendar.getTime(),"VenAuthSystem");
                            }
                        });

                    }else{
                        Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                Bukkit.getBanList(BanList.Type.IP).addBan(getIP(data.getInt("ip")), "Nieudana weryfikacja venAuth", null,"VenAuthSystem");
                            }
                        });
                    }
                }
                else if(object.getString("type").compareTo("remove_ban")==0){
                    JSONObject data = object.getJSONObject("data");
                    Main.getInstance().getServer().getScheduler().runTask(Main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            Bukkit.getBanList(BanList.Type.IP).pardon(getIP(data.getInt("ip")));
                        }
                    });
                }
            }
            t.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getIP(int IP){
        return ((IP >> 24 ) & 0xFF) + "." +

                ((IP >> 16 ) & 0xFF) + "." +

                ((IP >>  8 ) & 0xFF) + "." +

                ( IP        & 0xFF);
    }


}
