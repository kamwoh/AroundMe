package my.edu.um.fsktm.aroundme;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import my.edu.um.fsktm.aroundme.adapters.TabAdapter;
import my.edu.um.fsktm.aroundme.fragments.FragmentArticles;
import my.edu.um.fsktm.aroundme.fragments.FragmentBookmarks;
import my.edu.um.fsktm.aroundme.fragments.FragmentNotifications;

public class MyCollections extends AppCompatActivity {
    private Toolbar toolbar;
    private TabAdapter adapter;
    private TabLayout tabLayout;
    protected ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.icon_bookmarks,
            R.drawable.icon_articles,
            R.drawable.icon_notifications
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            case R.id.action_sign_out:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("signOut", true);
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
            case android.R.id.home:
                Log.d("MyCollections", "exit only");
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collections);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


        viewPager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        setupViewPager(viewPager);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        highLightCurrentTab(0, true);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                highLightCurrentTab(position, false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewPager.setOffscreenPageLimit(adapter.getCount());

    }


    private void highLightCurrentTab(int position, boolean init) {
        if (init) {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                assert tab != null;
                tab.setCustomView(null);
                tab.setCustomView(adapter.getTabView(i));
            }
        }

        TabLayout.Tab tab = tabLayout.getTabAt(position);
        assert tab != null;
        tab.setCustomView(null);
        tab.setCustomView(adapter.getSelectedTabView(position));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new TabAdapter(getSupportFragmentManager(), this);
        adapter.addFragment(new FragmentBookmarks(), "Bookmarks", tabIcons[0]);
        adapter.addFragment(new FragmentArticles(), "Articles", tabIcons[1]);
        adapter.addFragment(new FragmentNotifications(), "Notifications", tabIcons[2]);

    }


}
