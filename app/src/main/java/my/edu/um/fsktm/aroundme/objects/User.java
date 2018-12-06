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
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0) { // first time
                        userRef.child("name").setValue(user.name);
                    } else {
                        HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                        if (map != null) {
                            if (map.containsKey("posts")) {
                                HashMap<String, Boolean> postsMap;
                                postsMap = (HashMap<String, Boolean>) map.get("posts");
                                ArrayList<String> posts = user.posts;

                                for (String key : postsMap.keySet()) {
                                    if (postsMap.get(key) && !posts.contains(key))
                                        posts.add(key);
                                }
                            }

                            if (map.containsKey("bookmarks")) {
                                HashMap<String, Boolean> bookmarksMap;
                                bookmarksMap = (HashMap<String, Boolean>) map.get("bookmarks");
                                ArrayList<String> bookmarks = user.bookmarks;
                                for (String key : bookmarksMap.keySet()) {
                                    if (bookmarksMap.get(key) && !bookmarks.contains(key))
                                        bookmarks.add(key);
                                }
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
