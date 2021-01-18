package me.ventan;

import org.json.JSONObject;
import org.json.JSONTokener;
import me.ventan.utlis.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static me.ventan.utlis.Colors.*;

public class ClientHandler extends Thread {
    private volatile static List<ClientHandler> moblieUsers = new ArrayList<>();
    private volatile static List<ClientHandler> minecraftServers = new ArrayList<>();
    private volatile static List<ClientHandler> unknownUsers = new ArrayList<>();

    private Socket connection;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Thread timeoutThread;

    public ClientHandler(Socket connection) throws IOException {
        connection.setKeepAlive(true);
        this.connection = connection;
        dis = new DataInputStream(connection.getInputStream());
        dos = new DataOutputStream(connection.getOutputStream());
        this.start();
        Thread timeout = new Thread(() -> {
            ClientHandler clientHandler = this;
            try {
                Thread.currentThread().setName("auhorization thread for "+connection.getRemoteSocketAddress().toString());
                Thread.sleep(10000);
                JSONObject authorization_fail = new JSONObject();
                authorization_fail.put("type", "authorisation_fail");
                dos.writeUTF(authorization_fail.toString());
                dos.flush();
                clientHandler.disconnect();
            } catch (InterruptedException | IOException e) {
                if (!(e instanceof InterruptedException))
                    Logger.getInstance().addLogs(e);
            }
        });
        timeout.start();
        timeoutThread = timeout;
        unknownUsers.add(this);
        this.setName("Client thread for "+connection.getRemoteSocketAddress().toString());
    }

    public static List<ClientHandler> getMoblieUsers() {
        synchronized (moblieUsers) {
            return moblieUsers;
        }
    }

    public static List<ClientHandler> getMinecraftServers() {
        synchronized (minecraftServers) {
            return minecraftServers;
        }
    }

    public DataOutputStream getOutputStream() {
        return dos;
    }

    private Socket getSocket() {
        return connection;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String temp = dis.readUTF();
                Logger.getInstance().addLogs(YELLOW+"Got: " + temp + " from " + connection.getRemoteSocketAddress());
                JSONObject message = new JSONObject(new JSONTokener(temp));
                String type = message.getString("type");
                switch (type) {
                    case "handshake": {
                        new HandshakeManager(this, message);
                        break;
                    }
                    case "info": {
                        new InfoManager(this, message);
                        break;
                    }
                    case "request": {
                        new RequestManager(this, message);
                        break;
                    }
                    case "ping":{
                        System.out.println("got ping from server");
                        break;
                    }
                }
            }
        } catch (IOException | SQLException e) {
            if (e instanceof EOFException || e instanceof SocketException) {
                Logger.getInstance().addLogs(RED+"User disconnected");
                try {
                    disconnect();
                } catch (IOException ex) {
                    Logger.getInstance().addLogs(e);
                }
            } else {
                Logger.getInstance().addLogs(e);
            }
        }
    }

    public boolean isAuthorized() {
        boolean bool1;
        boolean bool2;
        synchronized (minecraftServers) {
            bool1 = minecraftServers.contains(this);
        }
        synchronized (minecraftServers) {
            bool2 = moblieUsers.contains(this);
        }

        return bool1 || bool2;
    }

    public void disconnect() throws IOException {
        timeoutThread.interrupt();
        synchronized (minecraftServers) {
            minecraftServers.remove(this);
        }
        synchronized (moblieUsers) {
            moblieUsers.remove(this);
        }
        synchronized (unknownUsers) {
            unknownUsers.remove(this);
        }
        Logger.getInstance().addLogs(RED+"preforming actions on disconnecting user: " + connection.getRemoteSocketAddress().toString());
        connection.close();
        Thread.currentThread().interrupt();
    }

    public boolean isServer() {
        SocketAddress socketAddress = connection.getRemoteSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetAddress inetAddress = ((InetSocketAddress) socketAddress).getAddress();
            String ip = inetAddress.toString().split("/")[1];
            synchronized (minecraftServers) {
                for (ClientHandler server : minecraftServers) {
                    SocketAddress socketAddress1 = server.connection.getRemoteSocketAddress();
                    InetAddress inetAddress1 = ((InetSocketAddress) socketAddress1).getAddress();
                    String ip1 = inetAddress1.toString().split("/")[1];
                    if (ip1.compareTo(ip) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addToServer() {
        timeoutThread.interrupt();
        synchronized (unknownUsers) {
            unknownUsers.remove(this);
        }
        synchronized (minecraftServers) {
            minecraftServers.add(this);
        }
    }

    public void addToMobile() {
        timeoutThread.interrupt();
        synchronized (unknownUsers) {
            unknownUsers.remove(this);
        }
        synchronized (moblieUsers) {
            moblieUsers.add(this);
        }
    }

    public String getIP() {
        return connection.getRemoteSocketAddress().toString();
    }

    public static List<ClientHandler> getUnknownUsers() {
        return unknownUsers;
    }

    @Override
    public String toString() {
        return getSocket().getRemoteSocketAddress().toString();
    }
}