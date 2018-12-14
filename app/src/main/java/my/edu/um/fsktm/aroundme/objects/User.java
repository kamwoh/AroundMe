package my.edu.um.fsktm.aroundme.objects;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    public static User currentUser;

    public String userId;
    public String name;
    public ArrayList<String> posts;
    public ArrayList<String> bookmarks;

    private ArrayList<Comment> notifications;

    public User() {
    }

    public ArrayList<Comment> getNotifications() {
        Log.d("User", "get notifications " + notifications);
        return notifications;
    }

    public User(String email, String name) {
        this.userId = email;
        this.name = name;
        posts = new ArrayList<>();
        bookmarks = new ArrayList<>();
        notifications = new ArrayList<>();
    }

    private static void notificationsUpdate(final User user, final String articleId) {
        for (int i = 0; i < PlaceTypes.getCategories().length; i++) { // only load if new post added
            final String tag = PlaceTypes.getCategories()[i];
            Log.d("User", "articleId " + articleId);
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("articles")
                    .child(tag)
                    .child(articleId)
                    .child("comments")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (dataSnapshot.exists()) {
                                Log.d("User", "comment " + dataSnapshot.toString());
                                Comment cm = dataSnapshot.getValue(Comment.class);
                                Log.d("User", "Comments " + cm.userName + " " + cm.comment);
                                if (!cm.userId.equals(user.userId)) {
                                    cm.setArticleId(articleId);
                                    cm.setTag(tag);
                                    user.notifications.add(cm);
                                }
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        }
    }

    public static void restoreOrPush(DatabaseReference usersRef, final User user, final boolean isLogin) {
        final DatabaseReference userRef = usersRef.child(user.userId);
        Log.d("User", "isLogin " + isLogin);

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
                                Log.d("User", "updating posts");
                                HashMap<String, String> postsMap;
                                postsMap = (HashMap<String, String>) map.get("posts");
                                ArrayList<String> posts = user.posts;

                                posts.clear();

                                for (String key : postsMap.keySet()) {
                                    posts.add(postsMap.get(key));
                                    notificationsUpdate(user, postsMap.get(key));
                                }
                            }

                            if (map.containsKey("bookmarks")) {
                                Log.d("User", "updating bookmarks");
                                HashMap<String, Boolean> bookmarksMap;
                                bookmarksMap = (HashMap<String, Boolean>) map.get("bookmarks");
                                ArrayList<String> bookmarks = user.bookmarks;
                                bookmarks.clear();

                                for (String key : bookmarksMap.keySet()) {
                                    if (bookmarksMap.get(key))
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
//            userRef.child("posts").setValue(user.posts);
//            userRef.child("bookmarks").setValue(user.bookmarks);
        }
    }

}
