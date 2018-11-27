package my.edu.um.fsktm.aroundme.objects;

import java.util.HashMap;

public class Article {
    public String tag;
    public String articleId;
    public String author;
    public float averageRating;
    public Comment[] comments;
    public String cover;
    public String description;
    public String title;

    public Article(HashMap firebaseMap) {
        // construct variable
    }
}
