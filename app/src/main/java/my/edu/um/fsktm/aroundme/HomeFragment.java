package my.edu.um.fsktm.aroundme;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty public constructor
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("hereee", "came hereee");
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        LoginActivity loginActivity = (LoginActivity) getActivity();
        Toolbar toolbar = v.findViewById(R.id.my_toolbar);
        loginActivity.setSupportActionBar(toolbar);
        return v;
    }

}
