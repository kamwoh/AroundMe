package my.edu.um.fsktm.aroundme.objects;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    public static User currentUser;

    public String userId;
    public String name;
    public ArrayList<String> posts;
    public ArrayList<String> bookmarks;

    public User() {
    }

    public User(String email, String name) {
        this.userId = email;
        this.name = name;
        posts = new ArrayList<>();
        bookmarks = new ArrayList<>();
    }

    public static void restoreOrPush(DatabaseReference usersRef, final User user, final boolean isLogin) {
        final DatabaseReference userRef = usersRef.child(user.userId);

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
                                HashMap<String, String> postsMap;
                                postsMap = (HashMap<String, String>) map.get("posts");
                                ArrayList<String> posts = new ArrayList<>();

                                for (String key : postsMap.keySet()) {
                                    posts.add(postsMap.get(key));
                                }

                                user.posts.addAll(posts);
                            }

                            if (map.containsKey("bookmarks")) {
                                HashMap<String, String> bookmarksMap;
                                bookmarksMap = (HashMap<String, String>) map.get("bookmarks");
                                ArrayList<String> bookmarks = new ArrayList<>();

                                for (String key : bookmarksMap.keySet()) {
                                    bookmarks.add(bookmarksMap.get(key));
                                }

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
