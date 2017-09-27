package com.syberkeep.oneonone;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager viewPagerMain;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout mTabLayout;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.include_toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        viewPagerMain = (ViewPager) findViewById(R.id.view_pager_main);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerMain.setAdapter(viewPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        mTabLayout.setupWithViewPager(viewPagerMain);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendUserToStartActivity();
        }
    }

    public void sendUserToStartActivity(){
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.menu_account_settings:
                startActivity(new Intent(MainActivity.this, AccountSettingsActivity.class));
                break;
            case R.id.menu_all_users:
                Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(usersIntent);
                break;
            case R.id.menu_log_out:
                logOutUser();
                break;
        }

        return true;
    }

    private void logOutUser() {
        FirebaseAuth.getInstance().signOut();
        sendUserToStartActivity();
    }
}