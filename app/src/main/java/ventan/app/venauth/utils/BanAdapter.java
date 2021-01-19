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

public class BanAdapter extends ArrayAdapter<BanEntry> {
    public BanAdapter(Context context, ArrayList<BanEntry> requests) {
        super(context, 0, requests);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BanEntry banEntry = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.banprefab, parent, false);
        }
        // Lookup view for data population
        TextView type = (TextView) convertView.findViewById(R.id.bantype);
        TextView ip = (TextView) convertView.findViewById(R.id.ipvalban);

        Button acceptButton = (Button)convertView.findViewById(R.id.unbanbtn);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerConnector.getInstance().sendUnban(banEntry.getIPAsInt());
                LoggedActivity.getInstance().removeBan(String.valueOf(banEntry.getIPAsInt()));
                LoggedActivity.getInstance().removeBan(banEntry);
            }
        });

        // Populate the data into the template view using the data object
        type.setText(banEntry.getType());
        ip.setText(banEntry.getIP());
        // Return the completed view to render on screen
        return convertView;
    }
}
