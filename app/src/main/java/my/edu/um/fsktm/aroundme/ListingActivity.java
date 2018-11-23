package my.edu.um.fsktm.aroundme;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class ListingActivity extends AppCompatActivity {

    private ActionBarController abc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        abc = new ActionBarController(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
         return abc.onOptionsItemSelected(item, super.onOptionsItemSelected(item));
    }

}
