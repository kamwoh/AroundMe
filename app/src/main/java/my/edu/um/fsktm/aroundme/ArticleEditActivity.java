package my.edu.um.fsktm.aroundme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import my.edu.um.fsktm.aroundme.objects.Article;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;
import my.edu.um.fsktm.aroundme.objects.User;

public class ArticleEditActivity extends AppCompatActivity {
    // edit stuff
    private Button submit, addImage, addLocation;
    private LatLng placeLatLng;
    private Bitmap currentBitmap;
    private Uri filePath;
    private ImageView imageView;
    private ProgressBar progressBar;
    private View transparentScreen;

    // variables
    private String tag;
    private boolean edit;
    private boolean blockTouch;
    private double averageRating;
    private String articleId;

    // coe
    private final int PICK_IMAGE_REQUEST = 71;
    private final int PLACE_PICKER_REQUEST = 1;

    // maps
    private GoogleMap map;
    private SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_edit);

        final EditText title = findViewById(R.id.name);
        final EditText desc = findViewById(R.id.desc);

        tag = getIntent().getExtras().getString("tag");

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.article_view_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
            }
        });

        //TODO Firebase image upload – uncomment these
//        storage = FirebaseStorage.getInstance();
//        storageReference = storage.getReference();

        submit = findViewById(R.id.submit);
        addImage = findViewById(R.id.addImage);
        addLocation = findViewById(R.id.selectLocation);

        imageView = findViewById(R.id.imgView);

//        progressBar = findViewById(R.id.progress_bar);
//        transparentScreen = findViewById(R.id.transparent_screen);

//        progressBar.setVisibility(View.INVISIBLE);
//        transparentScreen.setVisibility(View.INVISIBLE);
        blockTouch = false;

        setTitle("Add New Article");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockTouch = true;
//                progressBar.setVisibility(View.VISIBLE);
//                transparentScreen.setVisibility(View.VISIBLE);

                final String titleText = title.getText().toString();
                final String descText = desc.getText().toString();

                if (titleText.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (descText.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (placeLatLng == null) {
                    Toast.makeText(getApplicationContext(), "Location cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (currentBitmap == null) {
                    Toast.makeText(getApplicationContext(), "Image cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    HashMap<String, Object> map = new HashMap<>();

                    final DatabaseReference newArticleRef;
                    if (!edit) {
                        newArticleRef = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("articles")
                                .child(tag)
                                .push();
                    } else {
                        newArticleRef = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("articles")
                                .child(tag)
                                .child(articleId);
                    }

                    map.put("tag", tag);
                    map.put("articleId", newArticleRef.getKey());
                    map.put("author", User.currentUser.userId);
                    map.put("title", titleText);
                    map.put("description", descText);
                    map.put("averageRating", 0.0);
                    map.put("cover", String.format("images/%s.jpg", newArticleRef.getKey())); // firebase
                    map.put("lat", placeLatLng.latitude);
                    map.put("lng", placeLatLng.longitude);

                    final Article article = new Article(map);
                    final SimpleArticle simpleArticle = article.toSimpleArticle();

                    if (!edit) {
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("users")
                                .child(User.currentUser.userId)
                                .child("posts")
                                .push()
                                .setValue(article.articleId);
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    FirebaseStorage.getInstance()
                            .getReferenceFromUrl("gs://aroundme-e717d.appspot.com")
                            .child("images")
                            .child(article.articleId + ".jpg")
                            .putBytes(data)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    newArticleRef.setValue(article)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(getApplicationContext(), "You have added a new place!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent();
                                                    intent.putExtra("articleId", newArticleRef.getKey());
                                                    intent.putExtra("recreate", true);
                                                    setResult(RESULT_OK, intent);
                                                    finish();
                                                }
                                            });

                                    FirebaseDatabase.getInstance()
                                            .getReference()
                                            .child("simple_articles")
                                            .child(tag)
                                            .child(article.articleId)
                                            .setValue(simpleArticle);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Upload failed, make sure you have internet!", Toast.LENGTH_SHORT).show();
//                                    progressBar.setVisibility(View.INVISIBLE);
//                                    transparentScreen.setVisibility(View.INVISIBLE);
                                }
                            });

                    Toast.makeText(getApplicationContext(), "Uploading...", Toast.LENGTH_LONG).show();
                }
            }
        });

        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ArticleEditActivity.this), PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                    Log.d("ArticleEditActivity", String.valueOf(e.getMessage()) + " ");
                    Toast.makeText(getApplicationContext(), "Unknown error happens", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


        edit = getIntent().getBooleanExtra("edit", false);

        if (edit) {
            articleId = getIntent().getStringExtra("articleId");

            setTitle("Edit Article");

            blockTouch = true;
//            progressBar.setVisibility(View.VISIBLE);
//            transparentScreen.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("articles")
                    .child(tag)
                    .child(articleId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final Article article = new Article((HashMap) dataSnapshot.getValue());
                            final SimpleArticle simpleArticle = article.toSimpleArticle();

                            blockTouch = false;
                            title.setText(article.title);
                            desc.setText(article.description);

                            simpleArticle.getBitmap(getApplicationContext(), new Runnable() {
                                @Override
                                public void run() {
                                    setImageView(simpleArticle.getBitmap(getApplicationContext()));
                                }
                            });

                            setMap(new LatLng(article.lat, article.lng));
                            averageRating = article.averageRating;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!blockTouch)
            return super.dispatchTouchEvent(ev);
        Log.d("ArticleEditActivity", "blockTouch " + blockTouch);
        return true;
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void setImageView(Bitmap bitmap) {
        double oldWidth = bitmap.getWidth();
        double oldHeight = bitmap.getHeight();
        double newHeight = 200;
        double newWidth = oldWidth / oldHeight * newHeight;
        currentBitmap = Bitmap.createScaledBitmap(bitmap, (int) newWidth, (int) newHeight, false);
        imageView.setImageBitmap(currentBitmap);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
        imageView.setLayoutParams(params);
    }

    @SuppressLint("MissingPermission")
    private void setMap(LatLng latLng) {
        placeLatLng = latLng;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(placeLatLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.setMyLocationEnabled(true);
        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(12));

        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        mapFragment.getView().setLayoutParams(params);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                setImageView(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            setMap(place.getLatLng());
        }
    }
}
