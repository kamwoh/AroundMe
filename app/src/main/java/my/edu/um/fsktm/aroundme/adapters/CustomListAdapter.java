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
import my.edu.um.fsktm.aroundme.objects.Article;

public class CustomListAdapter extends ArrayAdapter<Article> {
    private final Activity context;
    private ArrayList<Article> list;

    public CustomListAdapter(Activity context, ArrayList<Article> ArticleArrayList) {
        super(context, R.layout.custom_bookmarklist, ArticleArrayList);
        this.context = context;
        this.list = ArticleArrayList;
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

