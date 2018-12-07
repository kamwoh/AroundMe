package my.edu.um.fsktm.aroundme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import my.edu.um.fsktm.aroundme.adapters.CommentsListAdapter;
import my.edu.um.fsktm.aroundme.objects.Article;
import my.edu.um.fsktm.aroundme.objects.Comment;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;
import my.edu.um.fsktm.aroundme.objects.User;


public class ArticleViewActivity extends AppCompatActivity {

    private CommentsListAdapter adapter;
    private User user;
    private String tag;
    private String articleId;
    private boolean isAuthor;
    private Article article;
    private Menu menu;
    private MenuItem editItem;
    private boolean isFavorite = false;
    private DatabaseReference articleRef;
    private GoogleMap googleMap;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);

        user = User.currentUser;
        tag = getIntent().getExtras().getString("tag");
        articleId = getIntent().getExtras().getString("articleId");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.article_view_map);
        Log.d("ArticleViewActivity", getSupportFragmentManager().getFragments().toString());
        Log.d("ArticleViewActivity", String.valueOf(mapFragment));
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                ArticleViewActivity.this.googleMap = googleMap;
            }
        });

        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(user.userId)
                .child("bookmarks")
                .child(articleId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("ArticleViewActivity", "dataSnapshot is null " + dataSnapshot);

                        if (dataSnapshot.exists()) {
                            isFavorite = dataSnapshot.getValue(Boolean.class);
                        } else {
                            isFavorite = false;
                        }

                        Log.d("ArticleViewActivity", "isFavorite " + isFavorite);
                        Log.d("ArticleViewActivity", "menu is null " + (menu == null));

                        setFavoriteIcon();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        articleRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("articles")
                .child(tag)
                .child(articleId);

        populateCommentsList();

        final ScrollView scrollViewParent = findViewById(R.id.article_view_scroll_view);
        final ProgressBar progressBar = findViewById(R.id.article_view_progress_bar);

        scrollViewParent.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        View customView = findViewById(R.id.article_view_custom_view);

        // Merge the scroll function of ListView && ScrollView
        customView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        scrollViewParent.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        scrollViewParent.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scrollViewParent.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        final Button button = findViewById(R.id.article_view_rating_submit);
        final EditText review = findViewById(R.id.article_view_review);
        final RatingBar rate = findViewById(R.id.article_view_rating_bar);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DatabaseReference commentRef = articleRef.child("comments").push();
                User user = User.currentUser;
                String commentId = commentRef.getKey();
                Log.d("ArticleViewActivity", "did i push " + commentId);
                Comment cm = new Comment(commentId, user.userId, user.name, ((double) rate.getRating()), review.getText().toString());
                commentRef.setValue(cm)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                review.setText("");
                                rate.setRating(0);

                                articleRef.child("comments")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Log.d("ArticleViewActivity", "comments added on data change");
                                                // not a good way but no choice because we don't have a server :(
                                                long count = 0;
                                                double ratings = 0;

                                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                    Comment cm = data.getValue(Comment.class);
                                                    if (cm.rating != 0) {
                                                        ratings += cm.rating;
                                                        count += 1;
                                                    }
                                                }

                                                if (count != 0)
                                                    ratings = ratings / count;

                                                articleRef.child("averageRating")
                                                        .setValue(ratings);

                                                FirebaseDatabase.getInstance()
                                                        .getReference()
                                                        .child("simple_articles")
                                                        .child(tag)
                                                        .child(articleId)
                                                        .child("rating")
                                                        .setValue(ratings);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        });

            }
        });


        articleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("ArticleViewActivity", "articleRef " + dataSnapshot.getKey());
                final Article article = new Article((HashMap) dataSnapshot.getValue());
                ArticleViewActivity.this.article = article;

                isAuthor = article.author.equals(user.userId);

                if (isAuthor) {
                    editItem.setVisible(true);
                }

                TextView title = findViewById(R.id.article_view_title);
                TextView content = findViewById(R.id.article_view_content);
                final ImageView cover = findViewById(R.id.article_view_cover);
                final SimpleArticle simpleArticle = article.toSimpleArticle();

                getSupportActionBar().setTitle(article.title);

                title.setText(article.title);

                String text = article.description;

                if (text.length() == 0)
                    text = "No description.";

                content.setText(text);

                cover.setImageBitmap(simpleArticle.getBitmap(getApplicationContext(), new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ArticleViewActivity", "Set bitmap cover");
                        Bitmap bitmap = simpleArticle.getBitmap(getApplicationContext());
                        Log.d("ArticleViewActivity", "bitmap reference " + bitmap);
                        cover.setImageBitmap(bitmap);
                    }
                }));

                if (googleMap != null) { // not sure which one will ready first
                    latLng = new LatLng(article.lat, article.lng);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                    googleMap.getUiSettings().setZoomGesturesEnabled(true);
                    googleMap.setMyLocationEnabled(true);
                    googleMap.addMarker(markerOptions);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
                }

                scrollViewParent.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("ArticleViewActivity", "onCreateOptionsMenu");
        Log.d("ArticleViewActivity", "menu is null " + menu);
        Log.d("ArticleViewActivity", "is favourite " + isFavorite);
        Log.d("ArticleViewActivity", "is author " + isAuthor);

        getMenuInflater().inflate(R.menu.top_with_edit, menu);
        editItem = menu.findItem(R.id.action_edit);
        editItem.setVisible(false);

        this.menu = menu;

        setFavoriteIcon();

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("recreate", false)) {
                Log.d("ArticleViewActivity", "recreate");
                recreate();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(getApplicationContext(), ArticleEditActivity.class);
                intent.putExtra("tag", tag);
                intent.putExtra("edit", true);
                intent.putExtra("articleId", articleId);
                startActivityForResult(intent, 0);
                return true;

            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("users")
                        .child(user.userId)
                        .child("bookmarks")
                        .child(articleId)
                        .setValue(!isFavorite);

                return true;
            case R.id.action_sign_out:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("signOut", true);
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
            case android.R.id.home:
                Log.d("ArticleViewActivity", "exit only");
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void populateCommentsList() {
        final ListView listView = findViewById(R.id.article_view_list_view_reviews);
        final ArrayList<Comment> listOfReviews = new ArrayList<>();
        adapter = new CommentsListAdapter(this, listOfReviews);
        listView.setAdapter(adapter);

        articleRef
                .child("comments")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Comment comment = dataSnapshot.getValue(Comment.class);
                        listOfReviews.add(comment);
                        listView.invalidate();
                        setListViewHeightBasedOnChildren(listView);
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

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    protected MapView mMapView;

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }

        Log.d("ArticleViewActivity", "onResume");
        Log.d("ArticleViewActivity", "isFavorite " + isFavorite);
        Log.d("ArticleViewActivity", "menu is null " + menu);

        setFavoriteIcon();
    }

    private void setFavoriteIcon() {
        if (menu != null) { // if firebase is loaded first
            MenuItem menuItem = menu.findItem(R.id.action_favorite);
            if (menuItem != null) {
                if (isFavorite)
                    menuItem.setIcon(R.drawable.like_on);
                else
                    menuItem.setIcon(R.drawable.like_off);
            }
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("tag", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }
}
