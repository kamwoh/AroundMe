package my.edu.um.fsktm.aroundme;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import my.edu.um.fsktm.aroundme.fragments.GPlusFragment;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setVisibility(View.INVISIBLE);

        FragmentManager fm = getSupportFragmentManager();
        GPlusFragment.switchToGPlusFragment(fm, null, false);
    }
}
