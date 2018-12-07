package my.edu.um.fsktm.aroundme.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import my.edu.um.fsktm.aroundme.LoginActivity;
import my.edu.um.fsktm.aroundme.MyCollections;
import my.edu.um.fsktm.aroundme.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private static boolean displayedWelcomeBack = false;

    public HomeFragment() {
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        FragmentManager fm = getActivity().getSupportFragmentManager();

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_sign_out:
                Log.d("HomeFragment", "SignOut");
                displayedWelcomeBack = false;
                GPlusFragment.switchToGPlusFragment(fm, this, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!displayedWelcomeBack) {
            Toast.makeText(getActivity(), "Welcome back", Toast.LENGTH_LONG).show();
            displayedWelcomeBack = true;
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("HomeFragment", "onCreateView");
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        LoginActivity loginActivity = (LoginActivity) getActivity();
        Toolbar toolbar = loginActivity.findViewById(R.id.my_toolbar);
        toolbar.setVisibility(View.VISIBLE);
        loginActivity.setSupportActionBar(toolbar);
        loginActivity.getSupportActionBar().setTitle(loginActivity.getString(R.string.app_name));

        int[] buttonIds = {
                R.id.list_food_button,
                R.id.list_landmark_button,
                R.id.list_transportation_button,
                R.id.list_accommodation_button,
                R.id.list_my_collections_button,
                R.id.list_donate_button
        };

        int[] drawableIds = {
                R.drawable.fast_food,
                R.drawable.camera,
                R.drawable.train,
                R.drawable.bed,
                R.drawable.baseline_collections_bookmark_black_18dp,
                R.drawable.magnifier_tool
        };

        for (int i = 0; i < buttonIds.length; i++) {
            ImageButton button = v.findViewById(buttonIds[i]);
            button.setImageResource(drawableIds[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonOnClick(v);
                }
            });
        }

        return v;
    }

    public void buttonOnClick(View v) {
        String tag = "";
        switch (v.getId()) {
            case R.id.list_food_button:
                tag = "food";
                break;
            case R.id.list_landmark_button:
                tag = "landmark";
                break;
            case R.id.list_accommodation_button:
                tag = "accommodation";
                break;
            case R.id.list_transportation_button:
                tag = "transportation";
                break;
            case R.id.list_my_collections_button:
                tag = "my_collections";
                break;
            default: // donate
                break;
        }

        if (tag.length() != 0) {
            switchToListingFragment(tag);
        } else {
            // go to donate page
            switchToSearchFragment();
        }
    }

    private void switchToSearchFragment() {
        Bundle args = new Bundle();
        args.putString("tag", "");
        args.putString("title", "Search");

        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment fragment = new SearchFragment();
        fragment.setArguments(args);

        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // return to previous page
                .commit();
    }

    private void switchToListingFragment(String tag) {
        if (tag.equalsIgnoreCase("my_collections")) {
            Intent intent = new Intent(getActivity(), MyCollections.class);
            startActivityForResult(intent, 0);
        } else {
            Bundle args = new Bundle();
            args.putString("tag", tag);
            args.putString("title", tag.substring(0, 1).toUpperCase() + tag.substring(1));

            FragmentManager fm = getActivity().getSupportFragmentManager();
            Fragment fragment = new ListingFragment();
            fragment.setArguments(args);

            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null) // return to previous page
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                boolean signOut = data.getBooleanExtra("signOut", false);

                if (signOut) {
                    displayedWelcomeBack = false;
                    GPlusFragment.switchToGPlusFragment(getActivity().getSupportFragmentManager(), this, true);
                }
            }
        }
    }
}
