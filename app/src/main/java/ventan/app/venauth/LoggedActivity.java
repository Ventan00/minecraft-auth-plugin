package ventan.app.venauth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import ventan.app.venauth.utils.BanAdapter;
import ventan.app.venauth.utils.BanEntry;
import ventan.app.venauth.utils.RequestAdapter;
import ventan.app.venauth.utils.RequestEntry;
import ventan.app.venauth.utils.ServerConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

public class LoggedActivity extends AppCompatActivity  {

    public static LoggedActivity getInstance() {
        return instance;
    }

    public static LoggedActivity instance;

    ArrayList<RequestEntry> requests = new ArrayList<>();
    RequestAdapter req_adapter;

    ArrayList<BanEntry> bans = new ArrayList<>();
    BanAdapter ban_adapter;

    private ConstraintLayout main;
    private ConstraintLayout ban;
    private ConstraintLayout down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.e("logged","create");

        setContentView(R.layout.logged_activity);

        main = findViewById(R.id.requestactivity);
        ban = findViewById(R.id.banactivity);
        down = findViewById(R.id.downmenu);

        instance=this;

        //set reqAdapter
        req_adapter = new RequestAdapter(this,requests);
        ListView listViewReq = (ListView) findViewById(R.id.item_request);
        listViewReq.setAdapter(req_adapter);

        //set banAdapter
        ban_adapter = new BanAdapter(this,bans);
        ListView listViewBan = (ListView) findViewById(R.id.item_ban);
        listViewBan.setAdapter(ban_adapter);

        //set actions on navButtons
        ImageButton button = (ImageButton)findViewById(R.id.mainscreenbtn);
        button.setBackgroundColor(Color.argb(50,255,255,255));


        //set android buttons style
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.e("logged","resume");
        if(ServerConnector.getInstance()==null || !ServerConnector.getInstance().isAlive()) {
            ServerConnector.createConnection();
        }else{
            ServerConnector.getInstance().getRequests();
            ServerConnector.getInstance().getBans();
        }


        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.e("logged","pause");
        req_adapter.clear();
        ban_adapter.clear();
        try {
            ServerConnector.stopme();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addRequests(JSONArray data){
        for(int i=0;i<data.length();i++){
            try {
                JSONObject object = data.getJSONObject(i);
                RequestEntry entry = new RequestEntry(object.getInt("ip"),object.getString("nick"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        req_adapter.add(entry);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void addBans(JSONArray data){
        for(int i=0;i<data.length();i++){
            try {
                JSONObject object = data.getJSONObject(i);
                BanEntry entry = new BanEntry(object.getInt("ip"),object.getString("type"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ban_adapter.add(entry);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void addBan(JSONObject object){
        try {
            BanEntry entry = new BanEntry(object.getInt("ip"),object.getString("type"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ban_adapter.add(entry);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void addRequest(JSONObject object){
        try {
            RequestEntry entry = new RequestEntry(object.getInt("ip"),object.getString("nick"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    req_adapter.add(entry);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void removeBan(String ip){
        BanEntry entry=null;
        for(BanEntry b: bans){
            if(String.valueOf(b.getIPAsInt()).compareTo(ip)==0)
                entry=b;
        }
        if(entry!=null){
            bans.remove(entry);
            BanEntry finalEntry = entry;
            runOnUiThread(()->{
                ban_adapter.remove(finalEntry);
            });
        }
    }
    public void removeRequest(String ip){
        RequestEntry entry=null;
        for(RequestEntry r: requests){
            if(String.valueOf(r.getIPAsInt()).compareTo(ip)==0)
                entry=r;
        }
        if(entry!=null){
            requests.remove(entry);
            RequestEntry finalEntry = entry;
            runOnUiThread(()->{
                req_adapter.remove(finalEntry);
            });
        }
    }
    public void removeBan(BanEntry entry){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ban_adapter.remove(entry);
            }
        });
    }
    public void addBan(BanEntry entry){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ban_adapter.add(entry);
            }
        });
    }
    public void MainScreen (View view){
        ImageButton button1 = (ImageButton)findViewById(R.id.mainscreenbtn);
        button1.setBackgroundColor(Color.argb(50,255,255,255));
        ImageButton button2 = (ImageButton)findViewById(R.id.banlistbutton);
        button2.setBackgroundColor(Color.argb(0,255,255,255));
        main.setVisibility(View.VISIBLE);
        ban.setVisibility(View.INVISIBLE);

    }
    public void BanScreen (View view){
        ImageButton button1 = (ImageButton)findViewById(R.id.mainscreenbtn);
        button1.setBackgroundColor(Color.argb(0,255,255,255));
        ImageButton button2 = (ImageButton)findViewById(R.id.banlistbutton);
        button2.setBackgroundColor(Color.argb(50,255,255,255));
        ban.setVisibility(View.VISIBLE);
        main.setVisibility(View.INVISIBLE);
    }
    public void LoginScreen (View view){
        Intent intent = new Intent(this,LoginActivity.class);
        intent.putExtra("logout","true");
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        ServerConnector.getInstance().registerActivityChange();
        finish();
    }
    public void logout(){
        runOnUiThread(()->{
            Intent intent = new Intent(this,LoginActivity.class);
            intent.putExtra("logout","true");
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            ServerConnector.getInstance().registerActivityChange();
            try {
                ServerConnector.stopme();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        });
    }
}
