package com.codeholic.bottomnavigation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;

import com.codeholic.centernavigation.CenterNavBar;
import com.codeholic.centernavigation.OnMenuSelectedListener;
import com.codeholic.centernavigation.OnMenuStatusChangedListener;

public class MainActivity extends AppCompatActivity {


    private CenterNavBar navBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        navBar = (CenterNavBar) findViewById(R.id.nav);

        navBar.setMainMenu(R.color.purple_500,R.drawable.ic_baseline_close_24)
                .addSubMenu(R.drawable.ic_baseline_home_24)
                .addSubMenu(R.drawable.ic_baseline_folder_open_24)
                .addSubMenu(R.drawable.ic_baseline_settings_24,true)
                .addSubMenu( R.drawable.ic_baseline_home_24)
                .addSubMenu( R.drawable.ic_baseline_folder_open_24)
                .addSubMenu( R.drawable.ic_baseline_home_24)
                .addSubMenu( R.drawable.ic_baseline_folder_open_24)
                .addSubMenu( R.drawable.ic_baseline_home_24)
                .addSubMenu( R.drawable.ic_baseline_folder_open_24)

                .setOnMenuSelectedListener(new OnMenuSelectedListener() {

                    @Override
                    public void onMenuSelected(int index) {
                        if (index == 0) {
                            // Start a activity or fragment from here :

                        }
                    }

                }).setOnMenuStatusChangeListener(new OnMenuStatusChangedListener() {

                    @Override
                    public void onMenuOpened() {
                    }

                    @Override
                    public void onMenuClosed() {
                    }

                });
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        navBar.openMenu();
        return super.onMenuOpened(featureId, menu);
    }
}