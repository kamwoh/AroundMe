package my.edu.um.fsktm.aroundme.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.objects.Comment;

public class CustomNotiListAdapter extends ArrayAdapter<Comment> {
    private final Activity context;

    public Runnable onClick;

    public CustomNotiListAdapter(Activity context, ArrayList<Comment> comments) {
        super(context, R.layout.custom_articlelist, comments);
        this.context = context;
    }

    @NonNull
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        if (view == null) {
            view = inflater.inflate(R.layout.custom_notificationlist, null, true);
        }

        Comment cm = getItem(getCount() - 1 - position);

        if (cm != null) {
            TextView notification = (TextView) view.findViewById(R.id.noti);
            if (cm.getArticleId() != null) {
                String s = cm.userName + " commented on your posts: " + cm.comment;
                notification.setText(s);
            }
        }

        return view;

    }
}
