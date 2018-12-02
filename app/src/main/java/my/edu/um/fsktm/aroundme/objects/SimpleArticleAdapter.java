package my.edu.um.fsktm.aroundme.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.R;

public class SimpleArticleAdapter extends ArrayAdapter<SimpleArticle> {

    private StorageReference storageRef;

    public SimpleArticleAdapter(Context context, ArrayList<SimpleArticle> list) {
        super(context, 0, list);

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://aroundme-e717d.appspot.com");

        storageRef = storage.getReference();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SimpleArticle article = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_simple_article, parent, false);
        }

        // Lookup view for data population
        TextView title = convertView.findViewById(R.id.simpleArticleTitle);
        title.setText(article.title);

        final File localFile = new File(getContext().getFilesDir(), "images/" + article.title + ".jpg");
        final ImageView cover = convertView.findViewById(R.id.simpleArticleImage);

        RatingBar ratingBar = convertView.findViewById(R.id.simpleRating);
        ratingBar.setRating(article.rating.floatValue());

        Log.d("localFile", String.valueOf(localFile) + " " + localFile.exists());

        if (localFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            cover.setImageBitmap(bitmap);
        } else {
            if (article.cover.length() == 0)
                return convertView;

            localFile.getParentFile().mkdirs();

            if (article.cover.contains("http")) {
                ImageRequest request = new ImageRequest(article.cover,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                try {
                                    FileOutputStream fos = new FileOutputStream(localFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    cover.setImageBitmap(bitmap);
                                    cover.invalidate();
                                } catch (Exception e) {
                                    Log.d("fk error", "duno what to catch: " + e.getMessage());
                                }
                            }
                        },
                        0,
                        0,
                        ImageView.ScaleType.CENTER_INSIDE,
                        null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                try {
                                    Log.d("i dunno why error again", error.getMessage());
                                } catch (Exception e) {
                                    Log.d("wtf", e.getMessage());
                                }
                            }
                        });

                RequestQueue queue = Volley.newRequestQueue(convertView.getContext());
                queue.add(request);
            } else {
                storageRef.child(article.cover)
                        .getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                cover.setImageBitmap(bitmap);
                                cover.invalidate();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }


        // Return the completed view to render on screen
        return convertView;
    }

}
