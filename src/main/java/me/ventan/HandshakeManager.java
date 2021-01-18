package me.ventan;

import org.json.JSONObject;
import me.ventan.utlis.FileManager;
import me.ventan.utlis.Logger;

import java.io.IOException;

import static me.ventan.utlis.Colors.*;

public class HandshakeManager {
    ClientHandler caller;
    JSONObject message;
    public HandshakeManager(ClientHandler connection, JSONObject message) throws IOException {
        caller=connection;
        this.message=message;
        switch (message.getString("device")){
            case "mobile":{
                mobile();
                break;
            } case "server":{
                server();
                break;
            }
        }
    }

    private void server() {
        String password = message.getJSONObject("data").getString("password");
        JSONObject response = new JSONObject();
        response.put("type","handshake_result");
        if(password.compareTo(FileManager.getInstance().getPassword())==0){
            caller.addToServer();
            response.put("success",true);
        }else{
            response.put("success",false);
        }
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

    private void mobile() throws IOException {
        String password = message.getJSONObject("data").getString("password");
        JSONObject response = new JSONObject();
        response.put("type","handshake_result");
        if(password.compareTo(FileManager.getInstance().getPassword())==0){
            caller.addToMobile();
            response.put("success",true);
        }else{
            response.put("success",false);
        }
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
}