package com.example.avianquest;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Set up custom navigation listener
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment)
                    .getChildFragmentManager()
                    .getFragments().get(0);

            if (currentFragment instanceof SurveyFragment) {
                if (item.getItemId() == R.id.navigation_encyclopedia ||
                        item.getItemId() == R.id.navigation_records) {
                    return ((SurveyFragment) currentFragment).onExitSurvey(() -> {
                        navController.navigate(item.getItemId());
                        item.setChecked(true);
                    });
                }
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }
}