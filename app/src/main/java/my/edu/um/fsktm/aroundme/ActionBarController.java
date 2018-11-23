package my.edu.um.fsktm.aroundme;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class ActionBarController {

    private AppCompatActivity activity;

    public ActionBarController(AppCompatActivity activity) {
        this.activity = activity;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        return onOptionsItemSelected(item, true);
    }

    public boolean onOptionsItemSelected(MenuItem item, boolean defaultAction) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                Log.i("signout", "signoutttttttttt");
                switchToGPlusFragment(true);
                return true;
            default:
                return defaultAction;
        }
    }

    public void switchToGPlusFragment(boolean isSignOut) {
        FragmentManager fm = activity.getSupportFragmentManager();

        Fragment fragment = new GPlusFragment(isSignOut);
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
