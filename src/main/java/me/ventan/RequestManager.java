package me.ventan;

import org.json.JSONArray;
import org.json.JSONObject;
import me.ventan.utlis.Logger;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static me.ventan.utlis.Colors.*;

public class RequestManager {
    ClientHandler caller;
    JSONObject message;
    static volatile Map<String,Thread> verificationThreads = new HashMap<>();

    public RequestManager(ClientHandler connection, JSONObject message) throws IOException, SQLException {
        if(!connection.isAuthorized() && !connection.isServer()){
            JSONObject response = new JSONObject();
            response.put("type","autharisation_fail");
            connection.getOutputStream().writeUTF(response.toString());
            connection.getOutputStream().flush();
            Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
            connection.disconnect();
        }else {
            caller=connection;
            this.message=message;
            switch (message.getString("requestFor")){
                case "check":{
                    check();
                    break;
                }
                case "send_requests":{
                    sendRequests();
                    break;
                }
                case "send_bans":{
                    sendBans();
                    break;
                }
            }
        }
    }

    // TODO: 11.01.2021 przetestować
    private void sendBans() throws SQLException, IOException {
        JSONObject response = new JSONObject();
        response.put("type","ban_list");
        JSONArray data = new JSONArray();
        ResultSet set = MainServer.createStatement().executeQuery("SELECT * FROM tempBan");
        while (set.next()){
            JSONObject ip = new JSONObject();
            ip.put("type","temp");
            ip.put("ip",""+set.getInt(1));
            data.put(ip);
        }
        set = MainServer.createStatement().executeQuery("SELECT * FROM banList");
        while (set.next()){
            JSONObject ip = new JSONObject();
            ip.put("type","perm");
            ip.put("ip",""+set.getInt(1));
            data.put(ip);
        }
        response.put("data",data);
        new Thread(()->{
            try {
                caller.getOutputStream().writeUTF(response.toString());
                caller.getOutputStream().flush();
                Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
            } catch (IOException e) {
                Logger.getInstance().addLogs(e);
            }
        }).start();
    }

    // TODO: 11.01.2021 przetestowac 
    private void sendRequests() throws SQLException {
        JSONObject response = new JSONObject();
        ResultSet requests = Objects.requireNonNull(MainServer.createStatement()).executeQuery("SELECT * FROM request");
        JSONArray objects = new JSONArray();
        while(requests.next()){
            JSONObject request = new JSONObject();
            request.put("ip",requests.getLong("ip"));
            request.put("nick",requests.getString("nick"));
            objects.put(request);
        }
        response.put("type","request_list");
        response.put("data",objects);
        new Thread(()->{
            try {
                caller.getOutputStream().writeUTF(response.toString());
                caller.getOutputStream().flush();
                Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
            } catch (IOException e) {
                Logger.getInstance().addLogs(e);
            }
        }).start();
    }

    private void OLDcheck() throws SQLException, IOException {
        JSONObject response = new JSONObject();
        response.put("type","verify_status");

        JSONObject data = message.getJSONObject("data");
        int ip = Integer.parseUnsignedInt(data.getString("ip"));
        String nick = data.getString("nick");

        CallableStatement statement = MainServer.getSqlconnection().prepareCall("{call checkuser(?,?)}");
        statement.setInt("IN_ip",ip);
        statement.registerOutParameter("isPresent", Types.BOOLEAN);
        statement.execute();
        boolean result = statement.getBoolean("isPresent");
        if(result){
            Logger.getInstance().addLogs("User is present in tables");
            response.put("success",false);
            new Thread(()->{
                Thread.currentThread().setName("Server response Thread");
                try {
                    caller.getOutputStream().writeUTF(response.toString());
                    caller.getOutputStream().flush();
                    Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
                    caller.disconnect();
                } catch (IOException e) {
                    Logger.getInstance().addLogs(e);
                }
            }).start();
        }else{
            ResultSet isSuccess = MainServer.createStatement().executeQuery("SELECT COUNT(*) FROM acceptedIP WHERE ip = "+ip+" AND data >= NOW() - interval 2 hour;");
            isSuccess.next();
            if(isSuccess.getInt(1)!=0){
                response.put("success",false);
                new Thread(()->{
                    try {
                        caller.getOutputStream().writeUTF(response.toString());
                        caller.getOutputStream().flush();
                        Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
                        caller.disconnect();
                    } catch (IOException e) {
                        Logger.getInstance().addLogs(e);
                    }
                }).start();
                new Thread(()->{
                    Thread verificationThread = new Thread(()->{
                        try {
                            Thread.currentThread().setName("verification thread for "+integerToStringIP(ip));
                            Thread.sleep(900000);
                            Logger.getInstance().addLogs("No confirmation for ip: "+integerToStringIP(ip)+". temp Banning");
                            MainServer.createStatement().executeUpdate("INSERT INTO tempBan VALUES ("+ip+");");
                            MainServer.createStatement().executeUpdate("DELETE FROM request WHERE ip = "+ip+" ;");
                            verificationThreads.remove(String.valueOf(ip));

                            JSONObject firstMessage = new JSONObject();
                            JSONObject secondMessage = new JSONObject();
                            JSONObject banData = new JSONObject();
                            firstMessage.put("type","remove_request");
                            firstMessage.put("ip",ip);

                            secondMessage.put("type","add_ban");
                            banData.put("ip",ip);
                            banData.put("type","temp");
                            secondMessage.put("data",data);


                            ClientHandler.getMoblieUsers().forEach(clientHandler ->{
                                try {
                                    clientHandler.getOutputStream().writeUTF(firstMessage.toString());
                                    clientHandler.getOutputStream().flush();
                                    Logger.getInstance().addLogs(CYAN+"sending "+firstMessage.toString()+" to "+caller.getIP());
                                    clientHandler.getOutputStream().writeUTF(secondMessage.toString());
                                    clientHandler.getOutputStream().flush();
                                    Logger.getInstance().addLogs(CYAN+"sending "+secondMessage.toString()+" to "+caller.getIP());
                                } catch (IOException e) {
                                    Logger.getInstance().addLogs(e);
                                }
                            });
                            ClientHandler.getMinecraftServers().forEach(server->{
                                try {
                                    server.getOutputStream().writeUTF(secondMessage.toString());
                                    server.getOutputStream().flush();
                                    Logger.getInstance().addLogs(CYAN+"sending "+secondMessage.toString()+" to "+server.getIP());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (InterruptedException | SQLException e) {
                            if (e instanceof SQLException)
                                Logger.getInstance().addLogs(e);
                        }
                    });
                    verificationThread.start();
                    synchronized (verificationThreads){
                        verificationThreads.put(String.valueOf(ip),verificationThread);
                    }
                    JSONObject new_request = new JSONObject();
                    new_request.put("type","new_request");
                    JSONObject request_data = new JSONObject();
                    request_data.put("ip",ip);
                    request_data.put("nick",nick);
                    new_request.put("data",data);
                    ClientHandler.getMoblieUsers().forEach(client->{
                        try {
                            client.getOutputStream().writeUTF(new_request.toString());
                            client.getOutputStream().flush();
                            Logger.getInstance().addLogs(CYAN+"sending "+new_request.toString()+" to "+caller.getIP());
                        } catch (IOException e) {
                            Logger.getInstance().addLogs(e);
                        }
                    });
                }).start();
            }else{
                response.put("success",true);
                caller.getOutputStream().writeUTF(response.toString());
                caller.getOutputStream().flush();
                Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
                caller.disconnect();
            }
        }
    }

    private void check() throws SQLException, IOException {
        JSONObject data = message.getJSONObject("data");
        int ip = Integer.parseUnsignedInt(data.getString("ip"));
        String nick = data.getString("nick");

        CallableStatement statement = MainServer.getSqlconnection().prepareCall("{call newCheckUser(?,?,?)}");
        statement.setInt("IN_ip",ip);
        statement.setString("in_nick",nick);
        statement.registerOutParameter("outCode", Types.INTEGER);
        statement.execute();
        int resultCode = statement.getInt("outCode");
        System.out.println("outCode "+resultCode);
        switch (resultCode){
            case 0: { //start verification thread
                //wyslij sucess false
                JSONObject response = new JSONObject();
                response.put("type","verify_status");
                response.put("success",false);
                Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
                caller.getOutputStream().writeUTF(response.toString());
                caller.getOutputStream().flush();
                caller.disconnect();
                //wyślij add_request
                new Thread(()->{
                    Thread.currentThread().setName("thread sending check result to mobile users");
                    JSONObject new_request = new JSONObject();
                    new_request.put("type","new_request");
                    JSONObject request_data = new JSONObject();
                    request_data.put("ip",ip);
                    request_data.put("nick",nick);
                    new_request.put("data",data);
                    ClientHandler.getMoblieUsers().forEach(user->{
                        try {
                            Logger.getInstance().addLogs(CYAN+"sending "+new_request.toString()+" to "+user.getIP());
                            user.getOutputStream().writeUTF(new_request.toString());
                            user.getOutputStream().flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }).start();
                //stwórz i wystartuj verification thread
                Thread verificationThread = new Thread(()->{
                    try {
                        Thread.currentThread().setName("verification thread for "+integerToStringIP(ip));
                        Thread.sleep(900000);
                        Logger.getInstance().addLogs("No confirmation for ip: "+integerToStringIP(ip)+". temp Banning");

                        //removing request and adding tempban
                        MainServer.createStatement().executeUpdate("INSERT INTO tempBan VALUES ("+ip+");");
                        MainServer.createStatement().executeUpdate("DELETE FROM request WHERE ip = "+ip+" ;");

                        //send info about ban to all mobile users
                        JSONObject remove_request = new JSONObject();
                        JSONObject add_ban = new JSONObject();
                        JSONObject banData = new JSONObject();
                        remove_request.put("type","remove_request");
                        remove_request.put("ip",ip);

                        add_ban.put("type","add_ban");
                        banData.put("ip",ip);
                        banData.put("type","temp");
                        add_ban.put("data",data);

                        //wysłanie bana do serwerów
                        ClientHandler.getMinecraftServers().forEach(server->{
                            Logger.getInstance().addLogs(CYAN+"sending "+add_ban.toString()+" to "+server.getIP());
                            try {
                                server.getOutputStream().writeUTF(add_ban.toString());
                            } catch (IOException e) {
                                Logger.getInstance().addLogs(e);
                            }
                        });

                        //wysłanie bana do klientów
                        ClientHandler.getMoblieUsers().forEach(user->{
                            Logger.getInstance().addLogs(CYAN+"sending "+remove_request.toString()+" to "+user.getIP());
                            Logger.getInstance().addLogs(CYAN+"sending "+add_ban.toString()+" to "+user.getIP());
                            try {
                                user.getOutputStream().writeUTF(remove_request.toString());
                                user.getOutputStream().writeUTF(add_ban.toString());
                            } catch (IOException e) {
                                Logger.getInstance().addLogs(e);
                            }
                        });

                        synchronized (verificationThreads){
                            verificationThreads.remove(String.valueOf(ip));
                        }
                    } catch (InterruptedException | SQLException e) {
                        if(e instanceof SQLException)
                            Logger.getInstance().addLogs(e);
                    }

                });
                verificationThread.start();
                //dodaj verification thread do listy verification threads
                synchronized (verificationThreads){
                    verificationThreads.put(String.valueOf(ip),verificationThread);
                }
                break;
            }
            case 1: { //user banned/during verification
                JSONObject response = new JSONObject();
                response.put("type","verify_status");
                response.put("success",false);
                Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
                caller.getOutputStream().writeUTF(response.toString());
                caller.disconnect();
                break;
            }
            case 2: { //verification success
                JSONObject response = new JSONObject();
                response.put("type","verify_status");
                response.put("success",true);
                Logger.getInstance().addLogs(CYAN+"sending "+response.toString()+" to "+caller.getIP());
                caller.getOutputStream().writeUTF(response.toString());
                caller.disconnect();
                break;
            }
        }
    }

    private String integerToStringIP(int ip) {
        return ((ip >> 24 ) & 0xFF) + "." +

                ((ip >> 16 ) & 0xFF) + "." +

                ((ip >>  8 ) & 0xFF) + "." +

                ( ip        & 0xFF);
    }

    public static void stopVerificationThread(String ip){
        synchronized (verificationThreads){
            Thread thread = verificationThreads.get(ip);
            if(thread!=null) {
                thread.interrupt();
                verificationThreads.remove(ip);
            }
        }
    }


}