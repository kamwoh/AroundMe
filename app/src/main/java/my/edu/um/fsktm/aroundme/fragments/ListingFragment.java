package my.edu.um.fsktm.aroundme.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import my.edu.um.fsktm.aroundme.LoginActivity;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.SimpleArticleAdapter;
import my.edu.um.fsktm.aroundme.objects.Article;
import my.edu.um.fsktm.aroundme.objects.PlaceTypes;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListingFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private LoginActivity loginActivity;
    private ArrayList<SimpleArticle> simpleArticles;
    private ArrayList<String> simpleArticleIds;

    private FirebaseDatabase database;
    private DatabaseReference simpleTagRef;
    private DatabaseReference detailTagRef;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private GoogleApiClient googleApiClient;
    private GeoDataClient geoDataClient;
    private PlaceDetectionClient placeDetectionClient;

    private String tag;
    private ListView listView;

    private double lat, lng;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public ListingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(loginActivity, data);
                Log.i("success yahoooo", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(loginActivity, data);
                // TODO: Handle the error.
                Log.i("cb error", status.getStatusMessage());

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d("wtf why you cancel", "why you do this");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        FragmentManager fm = loginActivity.getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                GPlusFragment.switchToGPlusFragment(fm, this, true);
                return true;
            case R.id.action_search:
                Log.i("on search", "search");
                switchToSearchFragment();
                return true;
            case android.R.id.home:
                loginActivity.getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkPermissionAndLocations() {
        if (ActivityCompat.checkSelfPermission(loginActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(loginActivity, "permission blocked", Toast.LENGTH_SHORT).show();
            return false;
        }

        LocationManager lm = (LocationManager) loginActivity.getSystemService(Context.LOCATION_SERVICE);

        if (lm == null) {
            Toast.makeText(loginActivity, "please turn on gps and restart", Toast.LENGTH_SHORT).show();
            return false;
        }

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            Toast.makeText(loginActivity, "please turn on gps and restart", Toast.LENGTH_SHORT).show();
            return false;
        }

        lng = location.getLongitude();
        lat = location.getLatitude();

        return true;
    }

    private void loadFirebaseSimpleArticle(DataSnapshot child) {
        SimpleArticle simpleArticle = child.getValue(SimpleArticle.class);

        if (simpleArticle == null)
            return;

        Location childLocation = new Location(simpleArticle.title);
        childLocation.setLatitude(simpleArticle.lat);
        childLocation.setLongitude(simpleArticle.lng);

        Location currLocation = new Location("Current Location");
        currLocation.setLatitude(lat);
        currLocation.setLongitude(lng);

        if (childLocation.distanceTo(currLocation) < 5000) {
            simpleArticles.add(simpleArticle);
            simpleArticleIds.add(simpleArticle.getArticleId());
        }
    }

    private void readResultFromRequest(JSONObject result, int i) throws JSONException {
        final Article article = new Article(tag, result);

        if (simpleArticleIds.contains(article.articleId))
            return;

        geoDataClient.getPlacePhotos(article.articleId)
                .addOnSuccessListener(new OnSuccessListener<PlacePhotoMetadataResponse>() {
                    @Override
                    public void onSuccess(PlacePhotoMetadataResponse placePhotoMetadataResponse) {
                        PlacePhotoMetadataBuffer buffer = placePhotoMetadataResponse.getPhotoMetadata();

                        if (buffer.getCount() != 0) {
                            PlacePhotoMetadata photoMetadata = buffer.get(0);
                            final File localFile = new File(getContext().getFilesDir(), "images/" + article.articleId + ".jpg");
                            localFile.getParentFile().mkdirs();

                            geoDataClient.getPhoto(photoMetadata)
                                    .addOnSuccessListener(new OnSuccessListener<PlacePhotoResponse>() {
                                        @Override
                                        public void onSuccess(PlacePhotoResponse placePhotoResponse) {
                                            try {
                                                Bitmap bitmap = placePhotoResponse.getBitmap();
                                                FileOutputStream fos = new FileOutputStream(localFile);
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                            } catch (Exception e) {
                                                Log.d("file not found", e.getMessage());
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("gg cannot load", "wtf message " + e.getMessage());
                                        }
                                    });
                        } else {
                            Log.d("no google photo arhh", "buffer count " + buffer.getCount());
                        }

                        buffer.release();
                    }
                });

        article.constructCoverUrl(getString(R.string.google_maps_key));

        Article.pushToFirebase(
                simpleTagRef,
                detailTagRef,
                article,
                null,
                null);

//        if (simpleArticles.size() < 20) {
        simpleArticles.add(article.toSimpleArticle());
        simpleArticleIds.add(article.articleId);
//        }

        Log.d("request " + i, article.toString());
        Log.d("request " + i, result.toString());
    }

    private StringRequest getStringRequest() {

        double radius = 10000;
        String rankby = "prominence";

        String url = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?%s",
                String.format("key=%s&", getString(R.string.google_maps_key)) +
                        String.format("location=%s,%s&", lat, lng) +
                        String.format("radius=%s&", radius) +
                        String.format("rankby=%s&", rankby) +
                        String.format("keyword=%s", PlaceTypes.getTypes(tag)));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject reader = new JSONObject(response);
                    JSONArray results = reader.getJSONArray("results");
                    Log.d("how many results", String.valueOf(results.length()));

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        readResultFromRequest(result, i);
                    }

                    updateAdapter();
                    listView.invalidate();
                } catch (Exception e) {
                    Log.e("error cb", e.getMessage());
                    Toast.makeText(loginActivity, "Connection error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("request cb", "failed 9 jor: ");
            }
        });

        return stringRequest;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tag = getArguments().getString("tag");

        String title = getArguments().getString("title");

        loginActivity = (LoginActivity) getActivity();
        loginActivity.getSupportFragmentManager().addOnBackStackChangedListener(this);
        loginActivity.getSupportActionBar().setTitle(title);

        if (!checkPermissionAndLocations())
            return;

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        simpleTagRef = database.getReference("simple_articles/" + tag);
        detailTagRef = database.getReference("articles/" + tag);

        geoDataClient = Places.getGeoDataClient(loginActivity);
        placeDetectionClient = Places.getPlaceDetectionClient(loginActivity);

        simpleArticles = new ArrayList<>();
        simpleArticleIds = new ArrayList<>();

        simpleTagRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("children item", String.valueOf(dataSnapshot.getChildrenCount()));

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            loadFirebaseSimpleArticle(child);
                        }

                        Log.d("how many?", "its fucking " + simpleArticles.size());

                        if (simpleArticles.size() < 30) {
                            RequestQueue queue = Volley.newRequestQueue(loginActivity);
                            StringRequest stringRequest = getStringRequest();
                            queue.add(stringRequest);
                        } else {
                            updateAdapter();
                            listView.invalidate();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//        Toast.makeText(getContext(), "in tag " + tag, Toast.LENGTH_SHORT).show();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_with_search, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_listing, container, false);
        // Create the adapter to convert the array to views
        // Attach the adapter to a ListView
        listView = v.findViewById(R.id.simpleArticleList);

        String title = getArguments().getString("title");

        loginActivity.getSupportActionBar().setTitle(title);

        updateAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // go to article view
                // load detail from firebase using simplearticle
                // extra: store as cache in sqlite
                // pass detail into article view for display
                Toast.makeText(getContext(), "onclick " + position, Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    @Override
    public void onBackStackChanged() {
        boolean canGoBack = loginActivity.getSupportFragmentManager().getBackStackEntryCount() > 0;
        loginActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
    }

    private void updateAdapter() {

        Collections.sort(simpleArticles, new Comparator<SimpleArticle>() {
            @Override
            public int compare(SimpleArticle o1, SimpleArticle o2) {
                Location location1 = new Location(o1.title);
                location1.setLatitude(o1.lat);
                location1.setLongitude(o1.lng);

                Location location2 = new Location(o2.title);
                location2.setLatitude(o2.lat);
                location2.setLongitude(o2.lng);

                Location currentLocation = new Location("Current location");
                currentLocation.setLatitude(lat);
                currentLocation.setLongitude(lng);

                double distance1 = currentLocation.distanceTo(location1);
                double distance2 = currentLocation.distanceTo(location2);

                double distance = distance1 - distance2;

//                Log.d("distanceee", o1.title + " vs " + o2.title + " " + distance);

                return (int) distance;
            }
        });

        if (simpleArticles.size() > 0) {
            int lastIndex = Math.min(simpleArticles.size(), 30);
            simpleArticles = new ArrayList<>(simpleArticles.subList(0, lastIndex));
        }

        SimpleArticleAdapter adapter = new SimpleArticleAdapter(getActivity(), simpleArticles);
        listView.setAdapter(adapter);
    }

    private void switchToSearchFragment() {
        Bundle args = new Bundle();
        args.putString("tag", tag);
        args.putString("title", tag.substring(0, 1).toUpperCase() + tag.substring(1) + " Searching");

        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment fragment = new SearchFragment();
        fragment.setArguments(args);

        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // return to previous page
                .commit();
    }

}
