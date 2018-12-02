package my.edu.um.fsktm.aroundme.objects;

import com.google.firebase.database.DatabaseReference;

public class Comment {
    public String comment;
    public Double rating;
    public String user;

    private String comment_id;

    public static void pushToFirebase(DatabaseReference commentRef,
                                      Comment comment) {
        DatabaseReference thisRef = commentRef.child(comment.comment_id);

        thisRef.setValue(comment);
    }


}
