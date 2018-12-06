package my.edu.um.fsktm.aroundme.objects;

import com.google.firebase.database.DatabaseReference;

public class Comment {
    public String comment;
    public Double rating;
    public String userId;
    public String userName;

    private String commentId;

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
