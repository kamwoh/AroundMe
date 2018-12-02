package my.edu.um.fsktm.aroundme.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import my.edu.um.fsktm.aroundme.R;

public class CustomListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final Integer[] locationImg;
    private final String[] text;

    public CustomListAdapter(Activity context, Integer[] locationImg, String[] text) {
        super(context, R.layout.custom_bookmarklist, text);
        this.context = context;
        this.locationImg = locationImg;
        this.text = text;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View rowView = inflater.inflate(R.layout.custom_bookmarklist, null, true);

        ImageView img = (ImageView) rowView.findViewById(R.id.bookmark_img);
        TextView location = (TextView) rowView.findViewById(R.id.bookmark_location);

        img.setImageResource(locationImg[position]);
        location.setText(text[position]);
        return rowView;

    }
}

