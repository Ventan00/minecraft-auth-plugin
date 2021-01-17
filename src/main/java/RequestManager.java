import org.json.JSONArray;
import org.json.JSONObject;
import utlis.Logger;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestManager {
    ClientHandler caller;
    JSONObject message;
    static volatile Map<String,Thread> verificationThreads = new HashMap<>();

    public RequestManager(ClientHandler connection, JSONObject message) throws IOException, SQLException {
        if(!connection.isAuthorized() && !connection.isServer()){
            JSONObject response = new JSONObject();
            response.put("type","autharisation_fail");
            connection.getOutputStream().writeUTF(response.toString());
            Logger.getInstance().addLogs("sending "+response.toString()+" to "+caller.getIP());
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
                Logger.getInstance().addLogs("sending "+response.toString()+" to "+caller.getIP());
            } catch (IOException e) {
                Logger.getInstance().addLogs(e);
            }
        }).start();
    }

    // TODO: 11.01.2021 przetestowac 
    private void sendRequests() throws SQLException {
        JSONObject response = new JSONObject();
        Logger.getInstance().addLogs("sendRequest runned");
        ResultSet requests = Objects.requireNonNull(MainServer.createStatement()).executeQuery("SELECT * FROM request");
        Logger.getInstance().addLogs("sendRequest querry came back");
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
                Logger.getInstance().addLogs("Got response from sql, prepering to send data "+response.toString());
                caller.getOutputStream().writeUTF(response.toString());
                Logger.getInstance().addLogs("sending "+response.toString()+" to "+caller.getIP());
            } catch (IOException e) {
                Logger.getInstance().addLogs(e);
            }
        }).start();
    }

    // TODO: 11.01.2021 przetestowac
    private void check() throws SQLException, IOException {
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
                try {
                    caller.getOutputStream().writeUTF(response.toString());
                    Logger.getInstance().addLogs("sending "+response.toString()+" to "+caller.getIP());
                    caller.disconnect();
                } catch (IOException e) {
                    Logger.getInstance().addLogs(e);
                }
            }).start();
        }else{
            ResultSet isSuccess = MainServer.createStatement().executeQuery("SELECT COUNT(*) FROM acceptedIP WHERE ip = "+ip+" AND data <= NOW() + interval 2 hour;");
            isSuccess.next();
            if(isSuccess.getInt(1)==0){
                response.put("success",false);
                new Thread(()->{
                    try {
                        caller.getOutputStream().writeUTF(response.toString());
                        Logger.getInstance().addLogs("sending "+response.toString()+" to "+caller.getIP());
                        caller.disconnect();
                    } catch (IOException e) {
                        Logger.getInstance().addLogs(e);
                    }
                }).start();
                new Thread(()->{
                    Thread verificationThread = new Thread(()->{
                        try {
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
                                    Logger.getInstance().addLogs("sending "+firstMessage.toString()+" to "+caller.getIP());
                                    clientHandler.getOutputStream().writeUTF(secondMessage.toString());
                                    Logger.getInstance().addLogs("sending "+secondMessage.toString()+" to "+caller.getIP());
                                } catch (IOException e) {
                                    Logger.getInstance().addLogs(e);
                                }
                            });
                            ClientHandler.getMinecraftServers().forEach(server->{
                                try {
                                    server.getOutputStream().writeUTF(secondMessage.toString());
                                    Logger.getInstance().addLogs("sending "+secondMessage.toString()+" to "+server.getIP());
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
                            Logger.getInstance().addLogs("sending "+new_request.toString()+" to "+caller.getIP());
                        } catch (IOException e) {
                            Logger.getInstance().addLogs(e);
                        }
                    });
                }).start();
            }else{
                response.put("success",true);
                caller.getOutputStream().writeUTF(response.toString());
                Logger.getInstance().addLogs("sending "+response.toString()+" to "+caller.getIP());
                caller.disconnect();
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