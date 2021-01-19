package ventan.app.venauth.utils;

public class BanEntry {
    int IP;
    String type;

    public BanEntry(int IP, String type){
        this.IP=IP;
        this.type = type;
    }

    public String getIP(){
        return ((IP >> 24 ) & 0xFF) + "." +

                ((IP >> 16 ) & 0xFF) + "." +

                ((IP >>  8 ) & 0xFF) + "." +

                ( IP        & 0xFF);
    }
    public String getType(){
        return type;
    }
    public int getIPAsInt(){
        return IP;
    }
}
