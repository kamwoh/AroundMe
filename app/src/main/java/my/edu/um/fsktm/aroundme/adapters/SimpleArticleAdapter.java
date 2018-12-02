package my.edu.um.fsktm.aroundme.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;

public class SimpleArticleAdapter extends ArrayAdapter<SimpleArticle> {

    private StorageReference storageRef;

    public SimpleArticleAdapter(Context context, ArrayList<SimpleArticle> list) {
        super(context, 0, list);

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://aroundme-e717d.appspot.com");

        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_simple_article, parent, false);
        }

        SimpleArticle article = getItem(position);

        if (article == null)
            return convertView;

        // Lookup view for data population
        TextView title = convertView.findViewById(R.id.simpleArticleTitle);
        title.setText(article.title);

        ImageView cover = convertView.findViewById(R.id.simpleArticleImage);

        RatingBar ratingBar = convertView.findViewById(R.id.simpleRating);
        ratingBar.setRating(article.rating.floatValue());

        Bitmap bitmap = article.getBitmap(getContext());

        Log.d("keep coming at:", "position " + position + "?????? bitmap " + String.valueOf(bitmap));

        if (bitmap != null) {
            cover.setImageBitmap(bitmap);
        }

        // Return the completed view to render on screen
        return convertView;
    }

}
