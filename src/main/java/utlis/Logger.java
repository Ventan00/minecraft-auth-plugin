package utlis;

import org.codehaus.plexus.util.ExceptionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Logger {
    List<String> logs = new ArrayList<>();
    private static Logger instance = new Logger();
    private Logger(){}
    public static Logger getInstance(){return instance;}
    public void addLogs(String logLine){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        logs.add("["+dtf.format(now)+"] "+logLine);
        System.out.println("["+dtf.format(now)+"] "+logLine);
    }

    public Thread saveLogs() {
        return new Thread(()->{
            File dir = new File("logs");
            if(!dir.exists())
                dir.mkdir();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            try {
                File file = new File("logs/log"+dtf.format(now)+".log");
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file);
                for(String line: logs){
                    fileWriter.append(line+"\n");
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void addLogs(String[] logLines){
        logs.addAll(Arrays.asList(logLines));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < logLines.length; i++) {
            addLogs(logLines[i]);
        }
    }
    public void addLogs(Exception e){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("["+dtf.format(now)+"] ");
        e.printStackTrace();
        logs.add(e.toString());
        logs.add(ExceptionUtils.getStackTrace(e));
    }
    public void addLogs(List<String> logList){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        logList.forEach(logLine->{
            logs.add("["+dtf.format(now)+"] "+logLine);
            System.out.println("["+dtf.format(now)+"] "+logLine);
        });
    }

}