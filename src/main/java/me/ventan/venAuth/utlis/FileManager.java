package me.ventan.venAuth.utlis;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static List<String> protectedNicks =  new ArrayList<>();
    private static FileManager instance =  new FileManager();
    private FileManager(){
        try (FileReader reader = new FileReader("ops.json"))
        {
            JSONTokener tokener = new JSONTokener(reader);
            JSONArray ops = new JSONArray(tokener);
            ops.forEach( op -> protectedNicks.add(((JSONObject)op).getString("name")));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static FileManager getInstance(){return instance;}
    public List<String> getProtectedNicks(){return protectedNicks;}
    public FileManager reread() {return new FileManager();}
}
