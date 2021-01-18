package me.ventan.utlis;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

import me.ventan.ClientHandler;

public class ConsoleManager extends Thread{
    private static ConsoleManager instance = new ConsoleManager();

    public static ConsoleManager getInstance() {
        return instance;
    }

    private ConsoleManager(){
        this.start();
        this.setName("Console Thread");
    }
    @Override
    public void run(){
        String input;
        Scanner scanner =  new Scanner(System.in);
        while( (input = scanner.nextLine()).compareTo("/stop")!=0){
            switch (input){
                case "/users":{
                    showUsers();
                    break;
                }
                case "/disconnectMobile":{
                    disconnectMobile();
                    break;
                }
                case "/threadlist":{
                    threadlist();
                    break;
                }
                case "/savelog":{
                    Logger.getInstance().saveLogs();
                    break;
                }
            }
        }
    }

    private void threadlist() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        threadSet.forEach(thread -> Logger.getInstance().addLogs(thread.getName()));
    }

    private void disconnectMobile() {
        ClientHandler.getMoblieUsers().forEach(ch -> {
            try {
                ch.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void showUsers() {
        Logger.getInstance().addLogs("MobileUsers: ");
        Logger.getInstance().addLogs(ClientHandler.getMoblieUsers());
        Logger.getInstance().addLogs("Servers: ");
        Logger.getInstance().addLogs(ClientHandler.getMinecraftServers());
        Logger.getInstance().addLogs("UnknownUsers: ");
        Logger.getInstance().addLogs(ClientHandler.getUnknownUsers());
    }
}
