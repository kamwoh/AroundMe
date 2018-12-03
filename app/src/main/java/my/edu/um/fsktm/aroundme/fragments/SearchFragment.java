package my.edu.um.fsktm.aroundme.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import my.edu.um.fsktm.aroundme.LoginActivity;
import my.edu.um.fsktm.aroundme.R;
import my.edu.um.fsktm.aroundme.adapters.SimpleArticleAdapter;
import my.edu.um.fsktm.aroundme.objects.PlaceTypes;
import my.edu.um.fsktm.aroundme.objects.SimpleArticle;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private DatabaseReference simpleTagRef;
    private LoginActivity loginActivity;
    private String tag;
    private EditText searchBar;
    private ImageButton searchButton;
    private ListView resultList;
    private ArrayList<SimpleArticle> results;

    public SearchFragment() {
        // Required empty public constructor
    }

    private void searchForTag(final String keyword, String tag) {
        Toast.makeText(loginActivity, "serachhh " + tag, Toast.LENGTH_SHORT).show();
        simpleTagRef = FirebaseDatabase.getInstance().getReference().child("simple_articles/" + tag);

        simpleTagRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("firebase key", dataSnapshot.getKey());
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for (String word : keyword.split(" ")) {
                        SimpleArticle simpleArticle = child.getValue(SimpleArticle.class);
                        if (simpleArticle != null && (simpleArticle.title + " " + simpleArticle.keyword).toLowerCase().contains(word.toLowerCase())) {
                            results.add(simpleArticle);
                            break;
                        }
                    }
                }

                Log.d("array list cb", results.toString());
                Toast.makeText(loginActivity, "foodddddddddddddd", Toast.LENGTH_SHORT).show();
                SimpleArticleAdapter adapter = new SimpleArticleAdapter(loginActivity, results);
                resultList.setAdapter(adapter);
                resultList.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onSearch() {
        String keyword = searchBar.getText().toString();

        Log.d("on click ciba", "yesssss");
        Log.d("cibai keyword", keyword + " tag " + tag);

        results = new ArrayList<>();

        if (tag.length() != 0) {
            searchForTag(keyword, tag);
        } else {
            for (int i = 0; i < PlaceTypes.getCategories().length; i++) {
                searchForTag(keyword, PlaceTypes.getCategories()[i]);
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

        loginActivity = (LoginActivity) getActivity();
        loginActivity.getSupportFragmentManager().addOnBackStackChangedListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        tag = getArguments().getString("tag");
        String title = getArguments().getString("title");

        loginActivity.getSupportActionBar().setTitle(title);

        View v = inflater.inflate(R.layout.fragment_search, container, false);

        searchButton = v.findViewById(R.id.fragment_search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearch();
            }
        });

        searchBar = v.findViewById(R.id.fragment_search_text);

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (actionId == KeyEvent.KEYCODE_ENTER)) {
                    onSearch();
                    return true;
                }
                return false;
            }
        });

        resultList = v.findViewById(R.id.fragment_search_list_view);

        return v;
    }

    @Override
    public void onBackStackChanged() {
        boolean canGoBack = loginActivity.getSupportFragmentManager().getBackStackEntryCount() > 0;
        loginActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

}
