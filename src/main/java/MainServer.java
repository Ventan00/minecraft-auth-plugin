import utlis.FileManager;
import utlis.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MainServer {
    private static Logger logger = Logger.getInstance();
    private static FileManager manager = FileManager.getInstance();


    volatile private static Connection sqlconnection;

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(logger.saveLogs());

        logger.addLogs("Starting authorisation server...");
        InetSocketAddress myAdress =  new InetSocketAddress(manager.getIp(),manager.getPort());
        ServerSocket server = new ServerSocket();
        server.bind(myAdress);
        logger.addLogs("Opening MySQL server connection...");
        Thread mysqlhandler = new Thread(()->{
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                sqlconnection = DriverManager.getConnection("jdbc:mysql://"+manager.getDb_server()+":"+manager.getDb_port()+"/"+manager.getDb_name(),manager.getDb_username(),manager.getDb_password());
                while(true){
                    Thread.sleep(58000);
                    System.out.println("Refreshing mysql connection");
                    sqlconnection = DriverManager.getConnection("jdbc:mysql://"+manager.getDb_server()+":"+manager.getDb_port()+"/"+manager.getDb_name(),manager.getDb_username(),manager.getDb_password());
                }

            } catch (ClassNotFoundException | SQLException | InterruptedException e) {
                logger.addLogs("MySQL connector exception");
                logger.addLogs(e);
                Logger.getInstance().addLogs("Shutting down...");
                System.exit(1);
            }
        });
        mysqlhandler.start();
        while(true){
            Socket connection = server.accept();
            logger.addLogs("User connected "+connection.getRemoteSocketAddress());
            new ClientHandler(connection);
        }
    }
    public static Statement createStatement() {
        try {
            return sqlconnection.createStatement();
        } catch (SQLException throwables) {
            Logger.getInstance().addLogs(throwables);
            return null;
        }
    }
    public static Connection getSqlconnection(){return sqlconnection;}
}