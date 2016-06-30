// ref: http://stackoverflow.com/questions/34097315/android-navigationview-from-right-to-left
// ref: http://stackoverflow.com/questions/29807744/how-can-i-align-android-toolbar-menu-icons-to-the-left-like-in-google-maps-app
package com.taxiyab;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.taxiyab.common.MyToast;
import com.taxiyab.common.States;
import com.taxiyab.db.LinesDB;
import com.taxiyab.test_cases.TestCase;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static MainActivity This;
    //public static Menu menu;
    public static States program_state = States.STATE_MAP_MAP_STARTED;
    public static LinearLayout menu;
    View.OnClickListener menuOptionesClickListener;
    boolean isDrawerOpen = false;

    private static LinesDB db = null;
    public static LinesDB getDB(){
        if (db == null)
            db = new LinesDB(This);
        return db;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        This = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TestCase.LoadTestCaseToDb(MainActivity.getDB());

        /*Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
        }*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        DrawerLayout drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawer_layout,
                //R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                /*ImageView btnDrawer = (ImageView)(This.findViewById(R.id.btnDrawer));
                RotateAnimation anim = new RotateAnimation(0f, 90f, Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setDuration(700);
                btnDrawer.startAnimation(anim);
                anim.setFillAfter(true);*/
                isDrawerOpen = true;
            }

            @Override
            public void onDrawerClosed(View view) {
            }

            @Override
            public void onDrawerSlide (View drawerView, float slideOffset){
                ImageView btnDrawer = (ImageView)(This.findViewById(R.id.btnDrawer));
                slideOffset = slideOffset*10;
                if (slideOffset > 1)
                    slideOffset = 1;

                RotateAnimation anim = new RotateAnimation(slideOffset*90, slideOffset*90, Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setDuration(1);
                btnDrawer.startAnimation(anim);
                anim.setFillAfter(true);
                isDrawerOpen = false;
            }


        };

        drawer_layout.addDrawerListener(mDrawerToggle);

        findViewById(R.id.drawer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open right drawer
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.END);
            }
        });
        /*ImageView mnBack = (ImageView)findViewById(R.id.mnBack);
        mnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyToast.makeText(This, "mnBack", Toast.LENGTH_SHORT).show();
            }
        });*/

        menu = (LinearLayout) findViewById(R.id.mainMenu);
        /*menu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });*/
        /*setSupportActionBar(t);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);*/


        menuOptionesClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == menu.findViewById(R.id.mnBack)) {
                    if (program_state == States.STATE_MAP_ENDING_POINT_SELECTED) {
                        FragmentMap.This.backToStep1();
                    } else if (program_state == States.STATE_MAP_BOTH_WAYS_SELECTED) {
                        FragmentMap.This.backToStep2();
                    }
                }
            }
        };

        View v = menu.findViewById(R.id.mnBack);
        v.setVisibility(View.GONE);
        v.setOnClickListener(menuOptionesClickListener);

        Fragment fragment = new FragmentMap();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_user_back) {
            if (program_state == States.STATE_MAP_ENDING_POINT_SELECTED){
                FragmentMap.This.backToStep1();
            }
            else if (program_state == States.STATE_MAP_BOTH_WAYS_SELECTED){
                FragmentMap.This.backToStep2();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    long lastPress;
    @Override
    public void onBackPressed() {
        // Close Drawer if is open
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (program_state == States.STATE_MAP_ENDING_POINT_SELECTED){
            FragmentMap.This.backToStep1();
            return;
        }
        else if (program_state == States.STATE_MAP_BOTH_WAYS_SELECTED){
            FragmentMap.This.backToStep2();
            return;
        }


        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastPress;
        if(diff > 2000){
            MyToast.makeText(getBaseContext(), "جهت خروج، مجددا دکمه بازگشت را بفشارید", MyToast.LENGTH_LONG).show();
            lastPress = currentTime;
        }else{
            MyToast.cancelAll();
            super.onBackPressed();
            System.gc();
        }
    }
}
