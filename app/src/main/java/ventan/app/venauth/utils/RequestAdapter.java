package ventan.app.venauth.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import ventan.app.venauth.LoggedActivity;
import ventan.app.venauth.R;

import java.util.ArrayList;

public class RequestAdapter extends ArrayAdapter<RequestEntry> {
    public RequestAdapter( Context context, ArrayList<RequestEntry> requests) {
        super(context, 0, requests);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RequestEntry requestEntry = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.requestbrefab, parent, false);
        }
        // Lookup view for data population
        TextView nick = (TextView) convertView.findViewById(R.id.nickval);
        TextView ip = (TextView) convertView.findViewById(R.id.ipval);

        Button acceptButton = (Button)convertView.findViewById(R.id.acceptbtn);
        Button declineButton = (Button)convertView.findViewById(R.id.declinebnt);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerConnector.getInstance().sendAcceptance(requestEntry.getIPAsInt(),false);
                LoggedActivity.getInstance().removeRequest(String.valueOf(requestEntry.getIPAsInt()));
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerConnector.getInstance().sendAcceptance(requestEntry.getIPAsInt(),true);
                LoggedActivity.getInstance().removeRequest(String.valueOf(requestEntry.getIPAsInt()));
                LoggedActivity.getInstance().addBan(new BanEntry(requestEntry.IP,"perm"));
            }
        });

        // Populate the data into the template view using the data object
        nick.setText(requestEntry.getNick());
        ip.setText(requestEntry.getIP());
        // Return the completed view to render on screen
        return convertView;
    }
}
