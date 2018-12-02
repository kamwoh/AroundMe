package my.edu.um.fsktm.aroundme.fragments;


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
import my.edu.um.fsktm.aroundme.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    public HomeFragment() {
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        FragmentManager fm = getActivity().getSupportFragmentManager();

        switch (item.getItemId()) {
            case R.id.action_sign_out:
                Log.d("signout", "signout");
                GPlusFragment.switchToGPlusFragment(fm, this, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        Log.i("hereee", "came hereee");
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        LoginActivity loginActivity = (LoginActivity) getActivity();
        Toolbar toolbar = loginActivity.findViewById(R.id.my_toolbar);
        toolbar.setVisibility(View.VISIBLE);
        loginActivity.setSupportActionBar(toolbar);

        int[] buttonIds = {
                R.id.list_food,
                R.id.list_landmark,
                R.id.list_transportation,
                R.id.list_accommodation,
                R.id.list_my_collections,
                R.id.list_donate
        };

        for (int id : buttonIds) {
            ImageButton button = v.findViewById(id);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonOnClick(v);
                }
            });
        }

        Toast.makeText(getActivity(), "Welcome back", Toast.LENGTH_LONG).show();

        return v;
    }

    public void buttonOnClick(View v) {
        String tag = "";
        switch (v.getId()) {
            case R.id.list_food:
                tag = "food";
                break;
            case R.id.list_landmark:
                tag = "landmark";
                break;
            case R.id.list_accommodation:
                tag = "accommodation";
                break;
            case R.id.list_transportation:
                tag = "transportation";
                break;
            case R.id.list_my_collections:
                tag = "my_collections";
                break;
            default: // donate
                break;
        }

        if (tag.length() != 0) {
            switchToListingFragment(tag);
        } else {
            // go to donate page
        }
    }

    private void switchToListingFragment(String tag) {
        Bundle args = new Bundle();
        args.putString("tag", tag);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment fragment = new ListingFragment();
        fragment.setArguments(args);

        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // return to previous page
                .commit();
    }

}
