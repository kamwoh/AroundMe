package my.edu.um.fsktm.aroundme.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;

public class CustomArticleListAdapter extends ArrayAdapter<SimpleArticle> {
    private final Activity context;

    public CustomArticleListAdapter(Activity context, ArrayList<SimpleArticle> simpleArticleArrayList) {
        super(context, R.layout.custom_articlelist, simpleArticleArrayList);
        this.context = context;
    }

    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        if (view == null) {
            view = inflater.inflate(R.layout.custom_articlelist, null, true);
        }

        final TextView comment = view.findViewById(R.id.article_comment);
        final ImageView commentImg = view.findViewById(R.id.article_img);
        final SimpleArticle simpleArticle = getItem(position);

        Log.d("CustomArticleAdapter", " " + simpleArticle);

        if (simpleArticle != null) {
            String s = "You added a new place named: " + simpleArticle.title;
            comment.setText(s);

            commentImg.setImageBitmap(simpleArticle.getBitmap(getContext(), new Runnable() {
                @Override
                public void run() {
                    commentImg.setImageBitmap(simpleArticle.getBitmap(getContext()));
                }
            }));
        }

        return view;

    }
}
