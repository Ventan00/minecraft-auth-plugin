package ventan.app.venauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ventan.app.venauth.utils.ServerConnector;

import org.json.JSONException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

public class LoginActivity extends AppCompatActivity {
    private static LoginActivity instance;
    String ip;
    int port;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("LoginData", MODE_PRIVATE);
        if(getIntent().hasExtra("logout")){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("ip");
            editor.remove("password");
            editor.remove("port");
            editor.apply();
        }
        ip = sharedPref.getString("ip",null);
        password = sharedPref.getString("password",null);
        port = sharedPref.getInt("port",0);
        if(ip!=null&&password!=null&&port!=0){
            //Log.e("loginActivity","są zapisane wartosci "+ip+" "+password+" "+port);
            try {
                ServerConnector.createConnection(ip,port,password);
            } catch (JSONException e) {
                e.printStackTrace();
                setContentView(R.layout.login_activity);
                TextView error = findViewById(R.id.errormessage);
                error.setVisibility(View.VISIBLE);
            }
        }else {
            setContentView(R.layout.login_activity);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static LoginActivity getInstance(){
        return instance;
    }

    public void login(View view){
        TextView error = findViewById(R.id.errormessage);
        error.setVisibility(View.INVISIBLE);

        TextView ipView = findViewById(R.id.iplogin);
        TextView passwordView = findViewById(R.id.haslologin);

        String ip = String.valueOf(ipView.getText()).trim();
        String password = String.valueOf(passwordView.getText()).trim();
        int port;
        if(ip.split(":").length!=2){
            port=7584;
        }else{
            port = Integer.parseInt(ip.split(":")[1]);
            ip = ip.split(":")[0];

            //set ip
            if(!validateIP(ip)){
                ip="127.0.0.1";
            }
        }

        this.ip=ip;
        this.password=password;
        this.port=port;

        try {
            ServerConnector.createConnection(ip,port,password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean validateIP(String ip) {
        Pattern p = Pattern.compile("(\\d{1,3}).(\\d{1,3}).(\\d{1,3}).(\\d{1,3})");
        Matcher m = p.matcher(ip);
        if(m.matches()){
            for (int i = 0; i < 4 ; i++) {
                if(Integer.valueOf(m.group(i))>255 || Integer.valueOf(m.group(i)) <0)
                    return false;
            }
            return true;
        }else {
            return false;
        }
    }

    public void LoginStatus(boolean success){
        if(success){
            runOnUiThread(()->{
                Context context = instance;
                SharedPreferences sharedPref = context.getSharedPreferences("LoginData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("ip",ip);
                editor.putString("password",password);
                editor.putInt("port",port);
                editor.apply();
                Intent intent = new Intent(this, LoggedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                ServerConnector.getInstance().registerActivityChange();
                finish();
            });
        }else{
            runOnUiThread(()->{
                TextView error = findViewById(R.id.errormessage);
                error.setVisibility(View.VISIBLE);
            });
        }
    }
}
