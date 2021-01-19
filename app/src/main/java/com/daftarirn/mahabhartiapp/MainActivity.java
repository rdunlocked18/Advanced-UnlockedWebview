package com.daftarirn.mahabhartiapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;



import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public boolean doubleBackToExitPressedOnce = false;

    private NavigationView navigationView;
    public Timer AdTimer;
    public WebView mWebView;
    private boolean open_from_push = false;
    Button buttonOk;
    // GCM
    public static final String PROPERTY_REG_ID = "notifyId";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    SharedPreferences preferences;
    String reg_cgm_id;
    static final String TAG = "MainActivity";
    private boolean first_fragment = false;
    private double latitude;
    private double longitude;

    @Override
    protected void onPause() {
        super.onPause();
        if (AdTimer != null) {
            AdTimer.cancel();
            AdTimer = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share_button:
                try {
                    mWebView = (WebView)findViewById(R.id.webView);

                    String urlshare = mWebView.getUrl();

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String sAux = getString(R.string.share_linker) + "\n";
                    sAux = sAux + urlshare + "\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "choose one"));
                } catch (Exception e) { //e.toString();
                }
                return true;

            case R.id.help:
                Bundle bundle1 = new Bundle();
                bundle1.putString("type", getString(R.string.help_type));
                bundle1.putString("url", getString(R.string.help_url));
                Fragment fragment1 = new FragmentWebInteractive();
                fragment1.setArguments(bundle1);
                FragmentManager fragmentManager1 = getSupportFragmentManager();
                fragmentManager1.beginTransaction().replace(R.id.frame_container, fragment1, "FragmentWebInteractive").commit();
                first_fragment = true;
                return true;

            case R.id.whatsapp:

                openWhatsApp("917769005566","कृपया मला व्हट्सअँप वर जॉब अपडेट्स पाठवा. Join <Your_Name>");

                return true;



                case R.id.rate:

                rateApp();


                //uncomment the code below to ENABLE dialog box for rating

                /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rate_app, viewGroup, false);
                builder.setView(dialogView);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                Button buttonOk = alertDialog.findViewById(R.id.buttonOk);
                //preserve review of ther user
                preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = preferences.edit();
                buttonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rateApp();
= (WebView)findViewById(R.id.webView);
                    }
                }); */
                return true;
            case R.id.re_home:
                Bundle bundle = new Bundle();
                bundle.putString("type", getString(R.string.home_type));
                bundle.putString("url", getString(R.string.home_url));
                Fragment fragment = new FragmentWebInteractive();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment, "FragmentWebInteractive").commit();
                first_fragment = true;
                return true;

            case R.id.retry :
                mWebView = (WebView)findViewById(R.id.webView);
                mWebView.reload();
                Toast.makeText(this, "Reloading Page...", Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (getString(R.string.rtl_version).equals("true")) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //to disable or enable drawer comment or uncomment the below 2 lines

        toggle.setDrawerIndicatorEnabled(false);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //to disable or enable drawer comment or uncomment the above 2 lines
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().getString("link", null) != null && !intent.getExtras().getString("link", null).equals("")) {
            open_from_push = true;
            String url = null;
            if (intent.getExtras().getString("link").contains("http")) {
                url = intent.getExtras().getString("link");
            } else {
                url = "http://" + intent.getExtras().getString("link");
            }

            Bundle bundle = new Bundle();
            bundle.putString("type", "url");
            bundle.putString("url", url);
            Fragment fragment = new FragmentWebInteractive();
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment, "FragmentWebInteractive").commit();
            first_fragment = true;

        } else if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString("type", getString(R.string.home_type));
            bundle.putString("url", getString(R.string.home_url));
            Fragment fragment = new FragmentWebInteractive();
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment, "FragmentWebInteractive").commit();
            first_fragment = true;
        }



        // Save token on server
       //sendRegistrationIdToBackend();

    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        Fragment webviewfragment = getSupportFragmentManager().findFragmentByTag("FragmentWebInteractive");
        if (webviewfragment instanceof FragmentWebInteractive) {
            if (((FragmentWebInteractive) webviewfragment).canGoBack()) {
                ((FragmentWebInteractive) webviewfragment).GoBack();


                return;
            }
        }

        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        } else {
            if (first_fragment == false) {
                super.onBackPressed();
            }
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1500);


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        String tag = null;
        first_fragment = false;

        if (id == R.id.home) {

            Bundle bundle = new Bundle();
            bundle.putInt("item_position", 0);
            bundle.putString("type", getString(R.string.home_type));
            bundle.putString("url", getString(R.string.home_url));
            fragment = new FragmentWebInteractive();
            fragment.setArguments(bundle);
            tag = "FragmentWebInteractive";
            first_fragment = true;

        } else if (id == R.id.about_us) {

            Bundle bundle = new Bundle();
            bundle.putInt("item_position", 1);
            bundle.putString("type", getString(R.string.about_us_type));
            bundle.putString("url", getString(R.string.about_us_url));
            fragment = new FragmentWebInteractive();
            fragment.setArguments(bundle);
            tag = "FragmentWebInteractive";

        } else if (id == R.id.portfolio) {

            Bundle bundle = new Bundle();
            bundle.putInt("item_position", 2);
            bundle.putSerializable("item_id", R.id.portfolio);
            bundle.putString("type", getString(R.string.portfolio_type));
            bundle.putString("url", getString(R.string.portfolio_url));
            fragment = new FragmentWebInteractive();
            fragment.setArguments(bundle);
            tag = "FragmentWebInteractive";

        } else if (id == R.id.contacts) {
            fragment = new FragmentContacts();
            tag = "FragmentContacts";
        }

        // ##################### --------------- EXAMPLE ----------------------- #################

        else if (id == R.id.nav_1) {
            Intent i = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(i);
            return true;

        } else if (id == R.id.nav_2) {
            // ---------------------------------  Load WebiView with Remote URL -------------------- //
            Bundle bundle = new Bundle();
            bundle.putString("type", getString(R.string.remote_type));
            bundle.putString("url", getString(R.string.remote_url));
            fragment = new FragmentWebInteractive();
            fragment.setArguments(bundle);
            tag = "FragmentWebInteractive";

        } else if (id == R.id.nav_3) {
            // ---------------------------------  Load WebiView with Remote URL -------------------- //
            Bundle bundle = new Bundle();
            bundle.putString("type", getString(R.string.interactive_type));
            bundle.putString("url", getString(R.string.interactive_url));
            fragment = new FragmentWebInteractive();
            fragment.setArguments(bundle);
            tag = "FragmentWebInteractive";

        } else if (id == R.id.nav_4) {
            // ---------------------------------  Load WebiView with Remote URL -------------------- //
            Bundle bundle = new Bundle();
            bundle.putString("type", getString(R.string.credits_type));
            bundle.putString("url", getString(R.string.credits_url));
            fragment = new FragmentWebInteractive();
            fragment.setArguments(bundle);
            tag = "FragmentWebInteractive";

        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment, tag).addToBackStack(null).commit();

        setTitle(item.getTitle());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void SetItemChecked(int position) {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(position).setChecked(true);
    }



    private void openWhatsApp(String numero,String mensaje){

        try{
            PackageManager packageManager = getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone="+ numero +"&text=" + URLEncoder.encode(mensaje, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i);
            }else {
                    Toast.makeText(getApplicationContext()," :) ",Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Log.e("ERROR WHATSAPP",e.toString());

        }

    }

    public void rateApp()
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);

            //store true on click disable button else boom !
            preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("rate_done",true);


        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String urlRate)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", urlRate, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

}

