import org.json.JSONObject;
import utlis.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class InfoManager {
    ClientHandler caller;
    JSONObject message;

    public InfoManager(ClientHandler connection, JSONObject message) throws IOException, SQLException {
        if (!connection.isAuthorized()) {
            JSONObject response = new JSONObject();
            response.put("type", "autharisation_fail");
            connection.getOutputStream().writeUTF(response.toString());
            Logger.getInstance().addLogs("sending " + response.toString() + " to " + caller.getIP());
            connection.disconnect();
        } else {
            caller = connection;
            this.message = message;
            switch (message.getString("info")) {
                case "acceptance": {
                    acceptance();
                    break;
                }
                case "unban": {
                    unban();
                    break;
                }
            }
        }
    }

    // TODO: 11.01.2021 przetestować
    private void acceptance() throws SQLException {
        JSONObject data = message.getJSONObject("data");
        String ip = data.getString("ip");
        boolean banOrNot = data.getBoolean("banOrNot");
        RequestManager.stopVerificationThread(ip);
        MainServer.createStatement().executeUpdate("DELETE FROM request WHERE ip = " + ip + " ;");
        if (!banOrNot) {
            MainServer.createStatement().executeUpdate("INSERT INTO banList VALUES (" + ip + ");");
            new Thread(() -> {
                JSONObject object = new JSONObject();
                object.put("type", "new_ban");
                JSONObject senddata = new JSONObject();
                senddata.put("ip", ip);
                senddata.put("type", "perm");
                object.put("data", senddata);
                List<ClientHandler> users = ClientHandler.getMoblieUsers();

                JSONObject object1 = new JSONObject();
                object1.put("type", "remove_request");
                JSONObject senddata1 = new JSONObject();
                senddata1.put("ip", ip);
                object1.put("data", senddata1);

                for (ClientHandler handler : users) {
                    if (handler == caller) {
                        continue;
                    } else {
                        try {
                            handler.getOutputStream().writeUTF(object1.toString());
                            Logger.getInstance().addLogs("sending " + object1.toString() + " to " + caller.getIP());
                            handler.getOutputStream().writeUTF(object.toString());
                            Logger.getInstance().addLogs("sending " + object.toString() + " to " + caller.getIP());
                        } catch (IOException e) {
                            Logger.getInstance().addLogs(e);
                        }
                    }
                }
                users = ClientHandler.getMinecraftServers();
                for (ClientHandler handler : users) {
                    try {
                        handler.getOutputStream().writeUTF(object.toString());
                        Logger.getInstance().addLogs("sending " + object.toString() + " to " + caller.getIP());
                    } catch (IOException e) {
                        Logger.getInstance().addLogs(e);
                    }
                }
            }).start();
        } else {
            MainServer.createStatement().executeUpdate("INSERT INTO acceptedIP VALUES (" + ip + ",CURRENT_TIMESTAMP );");
        }
        new Thread(() -> {
            List<ClientHandler> users = ClientHandler.getMoblieUsers();
            JSONObject object = new JSONObject();
            object.put("type", "remove_request");
            JSONObject senddata = new JSONObject();
            senddata.put("ip", ip);
            object.put("data", senddata);
            for (ClientHandler handler : users) {
                if (handler == caller) {
                    continue;
                } else {
                    try {
                        handler.getOutputStream().writeUTF(object.toString());
                        Logger.getInstance().addLogs("sending " + object.toString() + " to " + caller.getIP());
                    } catch (IOException e) {
                        Logger.getInstance().addLogs(e);
                    }
                }
            }
        }).start();
    }

    // TODO: 11.01.2021 przetestować
    private void unban() throws SQLException {
        JSONObject data = message.getJSONObject("data");
        String ip = data.getString("ip");
        MainServer.createStatement().executeUpdate("DELETE FROM tempBan WHERE ip = " + ip + " ;");
        MainServer.createStatement().executeUpdate("DELETE FROM banList WHERE ip = " + ip + " ;");
        new Thread(() -> {
            JSONObject object = new JSONObject();
            object.put("type", "remove_ban");
            JSONObject senddata = new JSONObject();
            senddata.put("ip", ip);
            object.put("data", senddata);
            List<ClientHandler> users = ClientHandler.getMoblieUsers();
            for (ClientHandler handler : users) {
                if (handler == caller) {
                    continue;
                } else {
                    try {
                        handler.getOutputStream().writeUTF(object.toString());
                        Logger.getInstance().addLogs("sending " + object.toString() + " to " + caller.getIP());
                    } catch (IOException e) {
                        Logger.getInstance().addLogs(e);
                    }
                }
            }
            users = ClientHandler.getMinecraftServers();
            for (ClientHandler handler : users) {
                try {
                    handler.getOutputStream().writeUTF(object.toString());
                    Logger.getInstance().addLogs("sending " + object.toString() + " to " + caller.getIP());
                } catch (IOException e) {
                    Logger.getInstance().addLogs(e);
                }
            }
        }).start();
    }
}