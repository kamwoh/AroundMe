package my.edu.um.fsktm.aroundme.objects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Article {
    public String title;
    public String description;
    public String tag;
    public double lat;
    public double lng;
    public String articleId;
    public String author;
    public double averageRating;
    public String cover;

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
        averageRating = 0;

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
        try {
            averageRating = (Double) firebaseMap.get("averageRating");
        } catch (Exception e) {
            averageRating = (Long) firebaseMap.get("averageRating");
        }

        lat = (double) firebaseMap.get("lat");
        lng = (double) firebaseMap.get("lng");

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

    public SimpleArticle toSimpleArticle() {
        SimpleArticle article = new SimpleArticle(author, title, averageRating, cover, lat, lng, PlaceTypes.getTypes(tag));
        article.setArticleId(articleId);
        return article;
    }

    public static void pushToFirebase(DatabaseReference simpleArticleRef,
                                      DatabaseReference detailArticleRef,
                                      final Article article,
                                      OnCompleteListener<Void> simpleOnCompleteListener,
                                      OnCompleteListener<Void> detailOnCompleteListener) {
        DatabaseReference newSimpleRow = simpleArticleRef.child(article.articleId);
        SimpleArticle simpleArticle = article.toSimpleArticle();
        Task<Void> simpleTask = newSimpleRow.setValue(simpleArticle);

        if (simpleOnCompleteListener != null)
            simpleTask.addOnCompleteListener(simpleOnCompleteListener);

        DatabaseReference newDetailRow = detailArticleRef.child(article.articleId);
        Task<Void> detailTask = newDetailRow.setValue(article);

        DatabaseReference commentRow = newDetailRow.child("comments");
        for (Comment comment : article.comments) {
            Comment.pushToFirebase(commentRow, comment);
        }

        if (detailOnCompleteListener != null)
            detailTask.addOnCompleteListener(detailOnCompleteListener);
    }

    private Bitmap createEmptyImage() {
        int width = 200, height = 200;
        int color = Color.TRANSPARENT;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }
}
