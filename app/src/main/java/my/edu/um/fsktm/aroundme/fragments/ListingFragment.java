package my.edu.um.fsktm.aroundme.fragments;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.LoginActivity;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.SimpleArticleAdapter;
import my.edu.um.fsktm.aroundme.objects.Article;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListingFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private LoginActivity loginActivity;
    private ArrayList<SimpleArticle> simpleArticles;

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
                Log.i("on search", "search");
                return true;
            case android.R.id.home:
                loginActivity.getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tag = getArguments().getString("tag");

        loginActivity = (LoginActivity) getActivity();
        loginActivity.getSupportFragmentManager().addOnBackStackChangedListener(this);

//        googleApiClient = new GoogleApiClient.Builder(loginActivity)
//                .addApi(Places.PLACE_DETECTION_API)
//                .addApi(Places.GEO_DATA_API)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(@Nullable Bundle bundle) {
//
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int i) {
//
//                    }
//                })
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//                    }
//                })
//                .enableAutoManage(loginActivity, R.string.server_client_id, new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//                    }
//                })
//                .build();
//        googleApiClient.connect();

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        simpleTagRef = database.getReference("simple_articles/" + tag);
        detailTagRef = database.getReference("articles/" + tag);


        geoDataClient = Places.getGeoDataClient(loginActivity);
        placeDetectionClient = Places.getPlaceDetectionClient(loginActivity);

        simpleTagRef.orderByChild("rating")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("children item", String.valueOf(dataSnapshot.getChildrenCount()));
                        if (ActivityCompat.checkSelfPermission(loginActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(loginActivity, "permission blocked", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        LocationManager lm = (LocationManager) loginActivity.getSystemService(Context.LOCATION_SERVICE);
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (location == null) {
                            Toast.makeText(loginActivity, "please turn on gps and restart", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double lng = location.getLongitude();
                        double lat = location.getLatitude();

                        simpleArticles = new ArrayList<>();

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            SimpleArticle simpleArticle = child.getValue(SimpleArticle.class);
                            Location childLocation = new Location(simpleArticle.title);
                            childLocation.setLatitude(simpleArticle.lat);
                            childLocation.setLongitude(simpleArticle.lng);

                            Location currLocation = new Location("Current Location");
                            currLocation.setLatitude(lat);
                            currLocation.setLongitude(lng);

                            if (childLocation.distanceTo(currLocation) < 5000) {
                                simpleArticles.add(simpleArticle);
                            }
                        }

                        if (simpleArticles.size() < 10) {
                            double radius = 10000;
                            String rankby = "prominence";

                            RequestQueue queue = Volley.newRequestQueue(loginActivity);
                            String url = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?%s",
                                    String.format("key=%s&", getString(R.string.google_maps_key)) +
                                            String.format("location=%s,%s&", lat, lng) +
                                            String.format("radius=%s&", radius) +
                                            String.format("rankby=%s&", rankby) +
                                            String.format("type=%s", "restaurant"));


                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject reader = new JSONObject(response);
                                        JSONArray results = reader.getJSONArray("results");
                                        Log.d("how many results", String.valueOf(results.length()));

                                        for (int i = 0; i < results.length(); i++) {
                                            JSONObject result = results.getJSONObject(i);
                                            Article article = new Article(tag, result);
                                            article.constructCoverUrl(getString(R.string.google_maps_key));

                                            Article.pushToFirebase(simpleTagRef,
                                                    detailTagRef,
                                                    article,
                                                    null,
                                                    null);

                                            simpleArticles.add(article.toSimpleArticle());

                                            Log.d("request " + i, article.toString());
                                            Log.d("request " + i, result.toString());
                                        }

                                        updateAdapter();
                                        listView.invalidate();
                                    } catch (Exception e) {
                                        Log.e("error cb", e.getMessage());
                                        Toast.makeText(loginActivity, "wtf json error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("request cb", "failed 9 jor: ");
                                }
                            });

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


        simpleArticles = new ArrayList<>();

        Toast.makeText(getContext(), "in tag " + tag, Toast.LENGTH_SHORT).show();

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
        SimpleArticleAdapter adapter = new SimpleArticleAdapter(getActivity(), simpleArticles);
        listView.setAdapter(adapter);
    }
}
