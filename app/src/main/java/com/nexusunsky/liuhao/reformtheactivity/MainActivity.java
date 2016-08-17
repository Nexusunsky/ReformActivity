package com.nexusunsky.liuhao.reformtheactivity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.nexusunsky.liuhao.reformtheactivity.activities.item.ActivitiesPrizeItemAdapter;
import com.nexusunsky.liuhao.reformtheactivity.tab.TabIndicator;
import com.nexusunsky.liuhao.reformtheactivity.tab.ViewHolderPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ViewHolderPagerAdapter.DataRecyclerAdapter mDataRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTab();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void initTab() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        List<String> datas = new ArrayList<>();
        datas.add("tab1");
        datas.add("tab2");
        datas.add("tab3");
        datas.add("tab4");
        datas.add("tab5");
        datas.add("tab6");

        mDataRecyclerAdapter = new ActivitiesPrizeItemAdapter(this);

        final ViewHolderPagerAdapter pagerAdapter =
                new ViewHolderPagerAdapter(datas, MainActivity.this, mDataRecyclerAdapter);

        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        initTabViewIndicator(viewPager, pagerAdapter, tabLayout);
    }

    private void initTabViewIndicator(ViewPager viewPager,
                                      ViewHolderPagerAdapter pagerAdapter,
                                      TabLayout tabLayout) {
        TabIndicator spIndicView = (TabIndicator) findViewById(R.id.custom_indicator);
        tabLayout.setTabsFromPagerAdapter(viewPager.getAdapter());
        spIndicView.setupWithTabLayout(tabLayout);
        spIndicView.setupWithViewPager(viewPager);
        customTabViewPager(viewPager, pagerAdapter, tabLayout);
    }

    private void customTabViewPager(
            final ViewPager viewPager,
            final ViewHolderPagerAdapter pagerAdapter,
            TabLayout tabLayout) {

        //1,设置TabLayout的选项卡监听：让ViewPager跟随Tablayout的选择而切换
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                ViewGroup customView = (ViewGroup) tab.getCustomView();
                customView.setSelected(true);//设置为Selected,让字体颜色改变
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        //2,设置Tab的标题来自PagerAdapter.getPageTitle()。
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);

        //3,设置TabLayout.TabLayoutOnPageChangeListener设置给ViewPager
        final TabLayout.TabLayoutOnPageChangeListener listener =
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout);

        viewPager.addOnPageChangeListener(listener);

        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(pagerAdapter.getTabView(i));
        }
    }
}
