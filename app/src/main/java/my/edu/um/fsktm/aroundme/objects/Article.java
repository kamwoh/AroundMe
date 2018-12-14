package my.edu.um.fsktm.aroundme.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Article implements Comparable<Article> {
    public String title;
    public String description;
    public String tag;
    public double lat;
    public double lng;
    public String articleId;
    public String author;
    public Double averageRating;
    public String cover;
    public String keyword;

    private boolean noPhoto = false;
    private Bitmap bitmap;
    private ArrayList<Comment> comments;
    private boolean coverUrlConstructed;
    private boolean fromFirebase;

    public Article(String tag, JSONObject jsonObject) throws JSONException {
        JSONObject location = jsonObject.getJSONObject("geometry").getJSONObject("location");
        lat = location.getDouble("lat");
        lng = location.getDouble("lng");

        articleId = jsonObject.getString("place_id");
        author = "Google";

        cover = jsonObject.has("photos") ?
                jsonObject.getJSONArray("photos").getJSONObject(0)
                        .getString("photo_reference") : "";
        coverUrlConstructed = false;
        fromFirebase = false;

        title = jsonObject.getString("name");
        description = "";
        averageRating = 0.0;
        keyword = PlaceTypes.getTypes(tag);

        comments = new ArrayList<>();

        this.tag = tag;
    }

    public void constructCoverUrl(String key) {
        if (cover.length() != 0 && !coverUrlConstructed && !fromFirebase) {

            cover = String.format("%s?%s",
                    "https://maps.googleapis.com/maps/api/place/photo",

                    String.format("maxwidth=%s&", 400) +
                            String.format("photoreference=%s&", cover) +
                            String.format("key=%s", key)
            );

            coverUrlConstructed = true;
        }
    }


    public Article(HashMap firebaseMap) {
        // construct variable
        Log.d("firebase map", firebaseMap.toString());
        articleId = (String) firebaseMap.get("articleId");
        tag = (String) firebaseMap.get("tag");
        title = (String) firebaseMap.get("title");
        description = (String) firebaseMap.get("description");
        author = (String) firebaseMap.get("author");
        cover = (String) firebaseMap.get("cover");
        keyword = (String) firebaseMap.get("keyword");
        try {
            averageRating = (Double) firebaseMap.get("averageRating");
        } catch (Exception e) {
            averageRating = ((Long) firebaseMap.get("averageRating")).doubleValue();
        }

        lat = (Double) firebaseMap.get("lat");
        lng = (Double) firebaseMap.get("lng");

//        if (firebaseMap.containsKey("comments")) { // not necessary as article view has a firebase controller already
//            HashMap<String, Object> commentsMap = (HashMap<String, Object>) firebaseMap.get("comments");
//            for (String key : commentsMap.keySet()) {
//                HashMap<String, Object> comment = (HashMap<String, Object>) commentsMap.get(key);
//                Comment cm = new Comment(key,
//                        comment.get("userId").toString(),
//                        comment.get("userName").toString(),
//                        ((Long) comment.get("rating")).doubleValue(),
//                        comment.get("comment").toString());
//                comments.add(cm);
//            }
//        }

        fromFirebase = true;
    }

    public String toString() {
        return String.format("tag=%s, author=%s, articleId=%s, title=%s, cover=%s, description=%s, averageRating=%s",
                tag,
                author,
                articleId,
                title,
                cover,
                description,
                averageRating);
    }

    public static void pushToFirebase(DatabaseReference detailArticleRef,
                                      final Article article,
                                      OnCompleteListener<Void> detailOnCompleteListener) {
        DatabaseReference newDetailRow = detailArticleRef.child(article.articleId);
        Task<Void> detailTask = newDetailRow.setValue(article);

        DatabaseReference commentRow = newDetailRow.child("comments");
        for (Comment comment : article.comments) {
            Comment.pushToFirebase(commentRow, comment);
        }

        if (detailOnCompleteListener != null)
            detailTask.addOnCompleteListener(detailOnCompleteListener);
    }

    public Bitmap createEmptyImage() {
        int width = 200, height = 200;
        int color = Color.TRANSPARENT;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }

    public Bitmap getBitmap(final Context context, final Runnable callback) {
        if (bitmap != null)
            return bitmap;

        final File localFile = new File(context.getFilesDir(), "images/" + articleId + ".jpg");
        Log.d("localFile", String.valueOf(localFile) + " " + localFile.exists());

        if (localFile.exists() && cover.contains("http")) {
            bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            return bitmap;
        }

        if (!noPhoto && cover.contains("http")) { // to confirm no photo
            final GeoDataClient geoDataClient = Places.getGeoDataClient(context);

            geoDataClient.getPlacePhotos(articleId)
                    .addOnSuccessListener(new OnSuccessListener<PlacePhotoMetadataResponse>() {
                        @Override
                        public void onSuccess(PlacePhotoMetadataResponse placePhotoMetadataResponse) {
                            PlacePhotoMetadataBuffer buffer = placePhotoMetadataResponse.getPhotoMetadata();

                            if (buffer.getCount() != 0) {
                                PlacePhotoMetadata photoMetadata = buffer.get(0);
                                localFile.getParentFile().mkdirs();

                                geoDataClient.getPhoto(photoMetadata)
                                        .addOnSuccessListener(new OnSuccessListener<PlacePhotoResponse>() {
                                            @Override
                                            public void onSuccess(PlacePhotoResponse placePhotoResponse) {
                                                try {
                                                    Bitmap bitmap = placePhotoResponse.getBitmap();
                                                    FileOutputStream fos = new FileOutputStream(localFile);
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                                    Article.this.bitmap = bitmap;
                                                    if (callback != null) {
                                                        callback.run();
                                                    }
                                                } catch (Exception e) {
                                                    // impossible reach here actually but java wan me put this zzzz
                                                    Log.d("SimpleArticle", e.getMessage());
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                noPhoto = true; // load fail = confirm no photo
                                                Log.d("SimpleArticle", " " + e.getMessage());
                                            }
                                        });
                            } else {
                                noPhoto = true; // this is google no photo!!!!!
                                Log.d("SimpleArticle", "buffer count " + buffer.getCount());
                            }

                            buffer.release();
                        }
                    });
        } else if (!noPhoto && cover.length() != 0) {
            localFile.getParentFile().mkdirs();
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl("gs://aroundme-e717d.appspot.com")
                    .child(cover)
                    .getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d("SimpleArticle", "Download from firebase done!");
                            bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            Log.d("SimpleArticle", "bitmap reference " + String.valueOf(bitmap));
                            if (callback != null) {
                                callback.run();
                            }
                        }
                    });
        }

        return createEmptyImage();
    }

    public Bitmap getBitmap(final Context context) {
        return getBitmap(context, null);
    }

    @Override
    public int compareTo(@NonNull Article o) {
        return (int) ((this.averageRating - o.averageRating) * 100);
    }

}
