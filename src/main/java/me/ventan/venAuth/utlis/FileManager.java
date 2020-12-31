package me.ventan.venAuth.utlis;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static List<String> protectedNicks =  new ArrayList<>();
    private static FileManager instance =  new FileManager();
    private FileManager(){
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("ops.json"))
        {
            Object obj = jsonParser.parse(reader);
            JSONArray ops = (JSONArray) obj;
            ops.forEach( op -> protectedNicks.add(((JSONObject)op).getString("name")));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }
    public static FileManager getInstance(){return instance;}
    public List<String> getProtectedNicks(){return protectedNicks;}
    public FileManager reread() {return new FileManager();}
}
