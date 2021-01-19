package ventan.app.venauth.utils;

import android.util.Log;

import ventan.app.venauth.LoggedActivity;
import ventan.app.venauth.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import ventan.app.venauth.LoginActivity;

public class ServerConnector extends Thread{
    private static String ip;
    private static int port;
    private static String password;

    private Socket mySocket;
    private Thread handShakeThread;

    private static ServerConnector instance = new ServerConnector();
    private DataOutputStream dos;
    private DataInputStream dis;

    private static boolean activity = true;

    private ServerConnector(){}
    public static void createConnection(){
        instance=new ServerConnector();
        instance.start();
    }
    public static ServerConnector getInstance(){return instance;}
    public static void createConnection(String ip, int port, String password) throws JSONException {
        ServerConnector.ip = ip;
        ServerConnector.port = port;
        ServerConnector.password = password;
        if(instance==null)
            instance=new ServerConnector();
        if(!instance.isAlive())
            instance.start();
    }

    public void registerActivityChange(){
        activity=!activity;
    }
    private void doHandshake() throws JSONException {
        JSONObject handshakeData = new JSONObject();
        handshakeData.put("type","handshake");
        handshakeData.put("device","mobile");
        JSONObject data = new JSONObject();
        data.put("password",password);
        handshakeData.put("data",data);
        new Thread(()->{
            try {
                dos.writeUTF(handshakeData.toString());
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                LoginActivity.getInstance().LoginStatus(false);
            }
        }).start();
    }

    public void sendAcceptance(int ip, boolean isBanned){
        try {
            JSONObject acceptance = new JSONObject();
            JSONObject data = new JSONObject();
            acceptance.put("type","info");
            acceptance.put("info","acceptance");
            data.put("ip",""+ip+"");
            data.put("banOrNot",isBanned);
            acceptance.put("data",data);
            new Thread(()->{
                try {
                    dos.writeUTF(acceptance.toString());
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendUnban(int ip){
        try {
            JSONObject unban = new JSONObject();
            JSONObject data = new JSONObject();
            unban.put("type","info");
            unban.put("info","unban");
            data.put("ip",""+ip+"");
            unban.put("data",data);
            new Thread(()->{
                try {
                    dos.writeUTF(unban.toString());
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getRequests(){
        try {
            JSONObject request = new JSONObject();
            request.put("type","request");
            request.put("requestFor","send_requests");
            new Thread(()->{
                try {
                    String temp = request.toString();
                    //Log.e("sending",temp);
                    dos.writeUTF(temp);
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getBans(){
        try {
            JSONObject request = new JSONObject();
            request.put("type","request");
            request.put("requestFor","send_bans");
            new Thread(()->{
                try {
                    String temp = request.toString();
                    //Log.e("sending",temp);
                    dos.writeUTF(temp.toString());
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            //("ServerConnector","Connecting to server");
            InetSocketAddress address = new InetSocketAddress(ip,port);
            mySocket = new Socket();
            mySocket.setKeepAlive(true);
            handShakeThread = new Thread(()->{
                try {
                    Thread.sleep(5000);
                    instance.interrupt();
                    //Log.e("ServerConnector","interupted due to handshake timeout");
                    mySocket.close();
                    instance = new ServerConnector();
                    LoginActivity.getInstance().LoginStatus(false);

                } catch (InterruptedException | IOException e) {
                }
            });
            handShakeThread.start();
            mySocket.connect(address);
            //Log.e("ServerConnector","connected");
            dos = new DataOutputStream(mySocket.getOutputStream());
            dis = new DataInputStream(mySocket.getInputStream());
            doHandshake();
            while (!Thread.currentThread().isInterrupted()){
                JSONObject object = new JSONObject(dis.readUTF());
                //Log.e("ServerConnector","Got messege from serwer: "+object.toString());
                switch (object.getString("type")){
                    case "new_request":{
                        new_request(object);
                        break;
                    }
                    case "new_ban":{
                        new_ban(object);
                        break;
                    }
                    case "remove_request":{
                        remove_request(object);
                        break;
                    }
                    case "remove_ban":{
                        remove_ban(object);
                        break;
                    }
                    case "request_list":{
                        request_list(object);
                        break;
                    }
                    case "ban_list":{
                        ban_list(object);
                        break;
                    }
                    case "handshake_result":{
                        handshake_result(object);
                        break;
                    }
                    case "autharisation_fail":{
                        autharisation_fail();
                        break;
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void handshake_result(JSONObject object) throws JSONException {
        if(activity)
            LoginActivity.getInstance().LoginStatus(object.getBoolean("success"));
        else {
            getRequests();
            getBans();
        }
        if(object.getBoolean("success"))
            handShakeThread.interrupt();
    }

    private void remove_ban(JSONObject object) throws JSONException {
        LoggedActivity.getInstance().removeBan(object.getJSONObject("data").getString("ip"));
    }

    private void remove_request(JSONObject object) throws JSONException {
        LoggedActivity.getInstance().removeRequest(object.getJSONObject("data").getString("ip"));
    }

    private void new_ban(JSONObject object) throws JSONException {
        LoggedActivity.getInstance().addBan(object.getJSONObject("data"));
    }

    private void new_request(JSONObject object) throws JSONException {
        LoggedActivity.getInstance().addRequest(object.getJSONObject("data"));
    }
    private void request_list(JSONObject object) throws JSONException {
        LoggedActivity.getInstance().addRequests(object.getJSONArray("data"));
    }
    private void ban_list(JSONObject object) throws JSONException {
        LoggedActivity.getInstance().addBans(object.getJSONArray("data"));
    }
    private void autharisation_fail() {
        LoggedActivity.getInstance().logout();
        Thread.currentThread().interrupt();
        try {
            stopme();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void stopme() throws IOException {
        instance.mySocket.close();
        instance.interrupt();
        instance = null;
    }
}
