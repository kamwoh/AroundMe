package my.edu.um.fsktm.aroundme.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.objects.Comment;

public class CommentsListAdapter extends ArrayAdapter<Comment> {
    public CommentsListAdapter(Context context, ArrayList<Comment> users) {
        super(context, 0, users);

//        Log.d("CommentsListAdapter")
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment, parent, false);
        }

        // Get the data item for this position
        Comment review = getItem(position);

        // Lookup view for data population
        TextView userId = convertView.findViewById(R.id.comment_name);
        TextView comment = convertView.findViewById(R.id.comment_comment);
        RatingBar rating = convertView.findViewById(R.id.comment_rating_bar);
        // Populate the data into the template view using the data object
        userId.setText(review.userName);
        comment.setText(review.comment);
        rating.setRating(review.rating.floatValue());

        // Return the completed view to render on screen
        return convertView;
    }
}
