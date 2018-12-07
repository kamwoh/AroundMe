package my.edu.um.fsktm.aroundme.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;

public class CustomListAdapter extends ArrayAdapter<SimpleArticle> {
    private final Activity context;
    private ArrayList<SimpleArticle> list;

    public CustomListAdapter(Activity context, ArrayList<SimpleArticle> simpleArticleArrayList) {
        super(context, R.layout.custom_bookmarklist, simpleArticleArrayList);
        this.context = context;
        this.list = simpleArticleArrayList;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        if (view == null) {
            view = inflater.inflate(R.layout.custom_bookmarklist, null, true);
        }

        final ImageView img = (ImageView) view.findViewById(R.id.bookmark_img);
        TextView location = (TextView) view.findViewById(R.id.bookmark_location);

        img.setImageBitmap(list.get(position).getBitmap(getContext(), new Runnable() {
            @Override
            public void run() {
                img.setImageBitmap(list.get(position).getBitmap(getContext()));
            }
        }));

        location.setText(list.get(position).title);

        return view;

    }
}

