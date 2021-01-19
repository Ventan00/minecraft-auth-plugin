package ventan.app.venauth.utils;


public class RequestEntry {
    int IP;
    String nick;
    public RequestEntry(int IP, String nick){
        this.IP=IP;
        this.nick = nick;
    }


    public String getIP(){
        return ((IP >> 24 ) & 0xFF) + "." +

                ((IP >> 16 ) & 0xFF) + "." +

                ((IP >>  8 ) & 0xFF) + "." +

                ( IP        & 0xFF);
    }
    public String getNick(){
        return nick;
    }
    public int getIPAsInt(){
        return IP;
    }
}
