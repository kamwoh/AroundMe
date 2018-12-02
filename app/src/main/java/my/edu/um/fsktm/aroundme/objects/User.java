package my.edu.um.fsktm.aroundme.objects;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {

    public String name;
    public ArrayList<String> posts;
    public ArrayList<String> bookmarks;
    private String email;

    public User() {
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        posts = new ArrayList<>();
        bookmarks = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public static void restoreOrPush(DatabaseReference usersRef, final User user, final boolean isLogin) {
        final DatabaseReference userRef = usersRef.child(user.email.replace(".com", ""));

        if (isLogin) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0) { // first time
                        userRef.child("name").setValue(user.name);
                    } else {
                        HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                        if (map != null) {
                            if (map.containsKey("posts")) {
                                List<String> posts = (List<String>) map.get("posts");
                                user.posts.addAll(posts);
                            }

                            if (map.containsKey("bookmarks")) {
                                List<String> bookmarks = (List<String>) map.get("bookmarks");
                                user.bookmarks.addAll(bookmarks);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            // not efficient but it is working :)
            userRef.child("posts").setValue(user.posts);
            userRef.child("bookmarks").setValue(user.bookmarks);
        }
    }

}
