package my.edu.um.fsktm.aroundme.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.LoginActivity;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;
import my.edu.um.fsktm.aroundme.objects.SimpleArticleAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListingFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private LoginActivity loginActivity;
    private ArrayList<SimpleArticle> simpleArticles;

    private FirebaseDatabase database;
    private DatabaseReference tagRef;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

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

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        tagRef = database.getReference(tag);

        tagRef.orderByChild("rating")
                .limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        simpleArticles = new ArrayList<>();

                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            SimpleArticle simpleArticle = child.getValue(SimpleArticle.class);
                            simpleArticles.add(simpleArticle);
                        }

                        // sort simpleArticles by ?

                        updateAdapter();
                        listView.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//        geoDataClient = Places.getGeoDataClient(loginActivity);
//        placeDetectionClient = Places.getPlaceDetectionClient(loginActivity);

//        String[] foodTypes = {"bakery", "cafe", "meal_delivery", "meal_takeaway", "restaurant"};

//        placeDetectionClient.getCurrentPlace(new PlaceFilter())

        simpleArticles = new ArrayList<>();

        Toast.makeText(getContext(), "in tag "+tag, Toast.LENGTH_SHORT).show();

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
                Toast.makeText(getContext(), "onclick "+position, Toast.LENGTH_SHORT).show();
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
