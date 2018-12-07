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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileOutputStream;

public class SimpleArticle implements Comparable<SimpleArticle> {

    public String author;
    public String title;
    public Double rating;
    public String cover;
    public String keyword = "";

    public Double lat;
    public Double lng;

    private String articleId;
    private Bitmap bitmap;
    private boolean noPhoto;

    public SimpleArticle() {

    }

    public SimpleArticle(String author, String title, Double rating, String cover, Double lat, Double lng, String keyword) {
        this.author = author;
        this.title = title;
        this.rating = rating;
        this.cover = cover;
        this.lat = lat;
        this.lng = lng;
        this.keyword = keyword;

        articleId = "";
        noPhoto = false;
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
                                                    SimpleArticle.this.bitmap = bitmap;
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
    public String toString() {
        return "title: " + title + ", rating: " + rating + ", cover: " + cover;
    }

    @Override
    public int compareTo(@NonNull SimpleArticle o) {
        return (int) ((this.rating - o.rating) * 100);
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
}
