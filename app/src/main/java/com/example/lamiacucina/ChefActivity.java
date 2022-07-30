package com.example.lamiacucina;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lamiacucina.activity.recipe.AddRecipeActivity;
import com.example.lamiacucina.activity.recipe.CustomizeRecipeActivity;
import com.example.lamiacucina.fragments.DemandListFragment;
import com.example.lamiacucina.fragments.HomeChefFragment;
import com.example.lamiacucina.fragments.KitchenLogFragment;
import com.example.lamiacucina.fragments.MealPlannerFragment;
import com.example.lamiacucina.fragments.MyFamilyFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChefActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    protected HomeChefFragment homeChefFragment = new HomeChefFragment(getSupportFragmentManager(), this);
    protected MealPlannerFragment mealPlannerFragment = new MealPlannerFragment(getSupportFragmentManager(), this);
    protected DemandListFragment demandListFragment = new DemandListFragment(this);
    protected KitchenLogFragment kitchenLogFragment = new KitchenLogFragment(this);
    protected MyFamilyFragment myFamilyFragment = new MyFamilyFragment(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);

        navigationView = findViewById(R.id.NavigationViewMainActivity);
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawers();
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                setHomeFragment();
                return true;
            } else if (id == R.id.nav_meal_planner) {
                setFragment(mealPlannerFragment);
                return true;
            } else if (id == R.id.addRecipe) {
                startActivity(new Intent(this, AddRecipeActivity.class));
                return true;
            } else if (id == R.id.nav_demand_list) {
                setFragment(demandListFragment);
                return true;
            } else if (id == R.id.nav_kitchen_log) {
                setFragment(kitchenLogFragment);
                return true;
            } else if (id == R.id.nav_family) {
                setFragment(myFamilyFragment);
                return true;
            } else if (id == R.id.logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChefActivity.this, StartActivity.class));
                finish();
                return true;
            } else
                return false;
        });

        setHomeFragment();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count>0)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.replace(R.id.flFragment, homeChefFragment,null);
            ft.commitAllowingStateLoss();
        }
        else {
            new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("EXIT")
                    .setContentText("Do you want to Exit App?")
                    .setConfirmText("Exit")
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismiss();
                        this.finish();
                        super.onBackPressed();
                    })
                    .setCancelButton("Cancel", SweetAlertDialog::dismissWithAnimation)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.flFragment, fragment).addToBackStack("tag");
        fragmentTransaction.commit();
    }

    private void setHomeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.flFragment, homeChefFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else if (item.getItemId() == R.id.notifications_button) {
            Intent intent = new Intent(ChefActivity.this, NotificationActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
    }
}