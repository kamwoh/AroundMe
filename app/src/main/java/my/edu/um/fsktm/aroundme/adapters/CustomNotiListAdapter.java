package my.edu.um.fsktm.aroundme.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import my.edu.um.fsktm.aroundme.R;

public class CustomNotiListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] text;

    public CustomNotiListAdapter(Activity context, String[] text) {
        super(context, R.layout.custom_articlelist, text);
        this.context = context;
        this.text = text;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        if (view == null) {
            view = inflater.inflate(R.layout.custom_notificationlist, null, true);
        }

        TextView notification = (TextView) view.findViewById(R.id.noti);

        notification.setText(text[position]);
        return view;

    }
}
