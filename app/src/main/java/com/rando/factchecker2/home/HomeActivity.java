package com.rando.factchecker2.home;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.rando.factchecker2.R;
import com.rando.factchecker2.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private AlertDialog dialog;

    private AppBarConfiguration appBarConfiguration;
    private ActivityHomeBinding binding;
    private HomeViewModel homeViewModel;
    private ActionsViewModel actionsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);



        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(WebContentHelper.getWebContent("https://en.wikipedia.org/wiki/World_War_I"));
                } catch (Exception e) {
                    Log.e("WebContentHelperError", e.getMessage());
                }
            }
        });
        thread.start();*/


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }








}