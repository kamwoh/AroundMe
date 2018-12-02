package my.edu.um.fsktm.aroundme;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collections);
        toolbar = (Toolbar) findViewById(R.id.MyCollectionsTB);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        setupViewPager(viewPager);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        highLightCurrentTab(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                highLightCurrentTab(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }


    private void highLightCurrentTab(int position) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            assert tab != null;
            tab.setCustomView(null);
            tab.setCustomView(adapter.getTabView(i));
        }

        TabLayout.Tab tab = tabLayout.getTabAt(position);
        assert tab != null;
        tab.setCustomView(null);
        tab.setCustomView(adapter.getSelectedTabView(position));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mycollectionstb, menu);
        return true;
    }

    private void setupViewPager(ViewPager viewPager){
        adapter = new TabAdapter(getSupportFragmentManager(), this);
        adapter.addFragment(new FragmentBookmarks(), "Bookmarks", tabIcons[0]);
        adapter.addFragment(new FragmentArticles(), "Articles", tabIcons[1]);
        adapter.addFragment(new FragmentNotifications(), "Notifications",tabIcons[2]);
    }



}
