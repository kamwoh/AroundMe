package my.edu.um.fsktm.aroundme.objects;

import com.google.firebase.database.DatabaseReference;

public class Comment {
    public String comment;
    public Double rating;
    public String userId;
    public String userName;

    private String commentId;
    private String tag;
    private String articleId;

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getTag() {
        return tag;
    }

    public String getArticleId() {
        return articleId;
    }

    public Comment() {
    }

    public Comment(String commentId, String userId, String userName, Double rating, String comment) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
    }

    public static void pushToFirebase(DatabaseReference commentRef,
                                      Comment comment) {
        DatabaseReference thisRef = commentRef.child(comment.commentId);

        thisRef.setValue(comment);
    }


}
