package my.edu.um.fsktm.aroundme.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import my.edu.um.fsktm.aroundme.ArticleEditActivity;
import my.edu.um.fsktm.aroundme.ArticleViewActivity;
import my.edu.um.fsktm.aroundme.LoginActivity;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.ArticleAdapter;
import my.edu.um.fsktm.aroundme.objects.Article;
import my.edu.um.fsktm.aroundme.objects.PlaceTypes;

import static my.edu.um.fsktm.aroundme.LoginActivity.ARTICLE_VIEW;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListingFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private LoginActivity loginActivity;
    private ArrayList<Article> articles;
    private ArrayList<String> articleIds;

    private FirebaseDatabase database;
    private DatabaseReference simpleTagRef;
    private DatabaseReference detailTagRef;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private GoogleApiClient googleApiClient;
    private GeoDataClient geoDataClient;
    private PlaceDetectionClient placeDetectionClient;
    private static Location lastKnownLocation;

    private String tag;
    private TextView noItemIndicator;
    private ListView listView;
    private FrameLayout frameLayout;
    private ProgressBar progressBar;
    private ArticleAdapter adapter;
    private boolean loadingGPS;

    public static double lat, lng;
    private final int ADD_NEW_PLACE = 12;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            lastKnownLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public ListingFragment() {
        // Required empty public constructor
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
                Log.i("ListingFragment", "on search");
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
            loginActivity.getSupportFragmentManager().popBackStack();
            return false;
        }

//        LocationManager lm = (LocationManager) loginActivity.getSystemService(Context.LOCATION_SERVICE);
//
//        if (lm == null) {
//            Log.d("ListingFragment", "location manager null");
//            Toast.makeText(loginActivity, "please turn on gps and restart", Toast.LENGTH_SHORT).show();
//            loginActivity.getSupportFragmentManager().popBackStack();
//            return false;
//        }
        if (!loadingGPS && lastKnownLocation == null) {
            FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(loginActivity);

            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        Log.d("ListingFragment", "lastKnownLocation: " + String.valueOf(lastKnownLocation));
                    } else {
                        Log.d("ListingFragment", "Current location is null. Using defaults.");
                        Log.e("ListingFragment", "Exception: %s", task.getException());
                        Toast.makeText(loginActivity, "please turn on gps and restart", Toast.LENGTH_SHORT).show();
                        loginActivity.getSupportFragmentManager().popBackStack();
                    }

                    loadingGPS = false;
                }
            });
            loadingGPS = true;
        } else if (lastKnownLocation != null) {
            loadingGPS = false;
        }

        Log.d("ListingFragment", "loadingGPS " + loadingGPS + " " + String.valueOf(lastKnownLocation));

        if (loadingGPS) {
            Toast.makeText(loginActivity, "please wait for a while for location reading", Toast.LENGTH_SHORT).show();
            loginActivity.getSupportFragmentManager().popBackStack();
            return false;
        }

        Log.d("ListingFragment", String.valueOf(lastKnownLocation));

//        if (lastKnownLocation == null) {
//            Log.d("ListingFragment", "location null");
//            Toast.makeText(loginActivity, "please wait for a while for location reading", Toast.LENGTH_SHORT).show();
//            loginActivity.getSupportFragmentManager().popBackStack();
//            return false;
//        }

        lng = lastKnownLocation.getLongitude();
        lat = lastKnownLocation.getLatitude();

        return true;
    }

    private void loadFirebaseSimpleArticle(DataSnapshot child) {
        if (child.getValue() == null)
            return;

        HashMap map = (HashMap) child.getValue();
        Article article = new Article(map);


        Location childLocation = new Location(article.title);
        childLocation.setLatitude(article.lat);
        childLocation.setLongitude(article.lng);

        Location currLocation = new Location("Current Location");
        currLocation.setLatitude(lat);
        currLocation.setLongitude(lng);

        if (childLocation.distanceTo(currLocation) < 5000) {
            articles.add(article);
            articleIds.add(article.articleId);
        }
    }

    private void readResultFromRequest(JSONObject result, int i) throws JSONException {
        final Article article = new Article(tag, result);

        if (articleIds.contains(article.articleId))
            return;

        article.constructCoverUrl(getString(R.string.google_maps_key));

        Article.pushToFirebase(
                detailTagRef,
                article,
                null);

        articles.add(article);
        articleIds.add(article.articleId);

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

                    if (results.length() == 0) {
                        progressBar.setVisibility(View.INVISIBLE);
                        noItemIndicator.setVisibility(View.VISIBLE);
                    } else {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject result = results.getJSONObject(i);
                            readResultFromRequest(result, i);
                        }

                        updateAdapter();
                        listView.invalidate();
                    }
                } catch (Exception e) {
                    Log.e("ListingFragment", e.getMessage());
                    Toast.makeText(loginActivity, "Connection error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ListingFragment", "failed 9 jor: ");
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

//        LocationManager locationManager = (LocationManager) loginActivity.getSystemService(LOCATION_SERVICE);
//
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000l, 100.0f, locationListener);
//
////        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(loginActivity);
////        Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
////
////        locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
////            @Override
////            public void onComplete(@NonNull Task<Location> task) {
////                if (task.isSuccessful()) {
////                    // Set the map's camera position to the current location of the device.
////                    lastKnownLocation = task.getResult();
////                    Log.d("ListingFragment", String.valueOf(lastKnownLocation));
////                } else {
////                    Log.d("ListingFragment", "Current location is null. Using defaults.");
////                    Log.e("ListingFragment", "Exception: %s", task.getException());
////                    Toast.makeText(loginActivity, "please turn on gps and restart", Toast.LENGTH_SHORT).show();
////                    loginActivity.getSupportFragmentManager().popBackStack();
////                }
////
////                loadingGPS = false;
////            }
////        });

        if (!checkPermissionAndLocations())
            return;

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        detailTagRef = database.getReference("articles/" + tag);

        geoDataClient = Places.getGeoDataClient(loginActivity);
        placeDetectionClient = Places.getPlaceDetectionClient(loginActivity);

        if (articles == null) {
            articles = new ArrayList<>();
            articleIds = new ArrayList<>();
        }

        detailTagRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("children item count", String.valueOf(dataSnapshot.getChildrenCount()));

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            loadFirebaseSimpleArticle(child);
                        }

                        Log.d("how many?", "is " + articles.size());

                        if (articles.size() < 30) {
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
        frameLayout = v.findViewById(R.id.listing_frame_layout);
        progressBar = v.findViewById(R.id.listing_progress_bar);
        noItemIndicator = v.findViewById(R.id.listing_no_item);

        frameLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        noItemIndicator.setVisibility(View.INVISIBLE);

        String title = getArguments().getString("title");

        loginActivity.getSupportActionBar().setTitle(title);

        if (articles == null) {
            articles = new ArrayList<>();
            articleIds = new ArrayList<>();
        }

        adapter = new ArticleAdapter(getActivity(), articles);
        listView.setAdapter(adapter);

        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(floatingButtonOnClick());

        updateAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // go to article view
                // load detail from firebase using simplearticle
                // extra: store as cache in sqlite
                // pass detail into article view for display
//                Toast.makeText(getContext(), "onclick " + position, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(loginActivity, ArticleViewActivity.class);
                intent.putExtra("tag", tag);
                intent.putExtra("articleId", articles.get(position).articleId);
                startActivityForResult(intent, ARTICLE_VIEW);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ARTICLE_VIEW && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                boolean signOut = data.getBooleanExtra("signOut", false);

                if (signOut)
                    GPlusFragment.switchToGPlusFragment(loginActivity.getSupportFragmentManager(), null, true);
            }
        }

        if (requestCode == ADD_NEW_PLACE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String articleId = data.getStringExtra("articleId");
                if (articleId != null) {
                    Intent intent = new Intent(loginActivity, ArticleViewActivity.class);
                    intent.putExtra("tag", tag);
                    intent.putExtra("articleId", articleId);
                    startActivityForResult(intent, ARTICLE_VIEW);
                }
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        boolean canGoBack = loginActivity.getSupportFragmentManager().getBackStackEntryCount() > 0;
        loginActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
    }

    private void updateAdapter() {
        Log.d("ListingFragment", "updateAdapter");
        Collections.sort(articles, new Comparator<Article>() {
            @Override
            public int compare(Article o1, Article o2) {
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

                return (int) distance;
            }
        });

        if (articles.size() > 0) {
            int lastIndex = Math.min(articles.size(), 30);

            for (int i = lastIndex; i < articles.size(); i++) {
                articles.remove(i);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    frameLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    noItemIndicator.setVisibility(View.INVISIBLE);
                }
            }, 1000);

            adapter.notifyDataSetChanged();
        }
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

    private View.OnClickListener floatingButtonOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ArticleEditActivity.class);
                intent.putExtra("tag", tag);
                intent.putExtra("edit", false);
                startActivityForResult(intent, ADD_NEW_PLACE);
            }
        };
    }

}
