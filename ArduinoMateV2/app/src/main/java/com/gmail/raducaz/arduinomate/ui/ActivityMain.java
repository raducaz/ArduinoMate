package com.gmail.raducaz.arduinomate.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionReset;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionSync;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.List;


/**
 * Provides UI for the main screen.
 */
public class ActivityMain extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LiveData<List<WorkInfo>> commandWorkerState;
        WorkManager mWorkManager = WorkManager.getInstance(this.getApplicationContext());
        commandWorkerState = mWorkManager.getWorkInfosByTagLiveData("COMMAND_WORKER");
        commandWorkerState.observe(this, listOfWorkInfos -> {});

        // Adding Toolbar to Main screen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set title of Detail page
        toolbar.setTitle("Home - All available functions");

        // Setting ViewPager for each Tabs
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Set Tabs inside Toolbar
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.addTab(tabs.newTab().setText("Logs"));
        tabs.addOnTabSelectedListener(onTabSelectedListener(viewPager));

        // Create Navigation drawer and inlfate layout
        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            VectorDrawableCompat indicator
                    = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(),R.color.white,getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Set item in checked state
                        menuItem.setChecked(true);

                        if(menuItem.getTitle().equals("Config"))
                        {
                            Intent intent = new Intent(getBaseContext(), ActivityConfigMain.class);
                            startActivity(intent);
                        }
                        if(menuItem.getTitle().equals("Settings"))
                        {
                            Intent intent = new Intent(getBaseContext(), ActivitySettingsMain.class);
                            startActivity(intent);
                        }

                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

//        // Start Service
//        Intent startServiceIntent = new Intent(this, TcpServerIntentService.class);
//        startService(startServiceIntent);

    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        /* Use the root fragment so it can be reused for the other tabs in the TabLayout */
        RootFragment initialFragment = new RootFragment();
        initialFragment.setReplacementFragment(new FragmentFunctionList());
        adapter.addFragment(initialFragment, "All");

        viewPager.setAdapter(adapter);
//        viewPager.setCurrentItem(0);
//        adapter.notifyDataSetChanged();

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }
        };
        viewPager.addOnPageChangeListener(pageChangeListener);
    }

    private TabLayout.OnTabSelectedListener onTabSelectedListener(final ViewPager viewPager) {

        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FragmentTransaction trans = getSupportFragmentManager()
                        .beginTransaction();
                /*
                 * IMPORTANT: We use the "root frame" defined in
                 * "root_fragment.xml" as the reference to replace fragment
                 */
                if(tab.getPosition()==0)
                    trans.replace(R.id.root_frame, new FragmentFunctionList());
                if(tab.getPosition()==1)
                    trans.replace(R.id.root_frame, new FragmentAllExecutionLogList());

                /*
                 * IMPORTANT: The following lines allow us to add the fragment
                 * to the stack and return to it later, by pressing back
                 */
//                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                trans.addToBackStack(null);

                trans.commit();

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logs) {

            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File logFile = new File(path + "/logger/logs_0.csv");
            Intent i = new Intent();
            i.setAction(android.content.Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(logFile), "text/csv");
            startActivity(i);

            return true;
        }else if (id==R.id.action_reset_all)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
            builder.setMessage(R.string.confirm_reset_all)
                    .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ArduinoMateApp application = (ArduinoMateApp) getApplication();
                            TaskFunctionReset functionReset = new TaskFunctionReset(application.getRepository());
                            new TaskExecutor().execute(functionReset);
                        }
                    })
                    .setNegativeButton(R.string.confirm_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            AlertDialog alert = builder.create();
            alert.show();

        }else if (id==R.id.action_sync_all)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
            builder.setMessage(R.string.confirm_sync_all)
                    .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ArduinoMateApp application = (ArduinoMateApp) getApplication();
                            TaskFunctionSync caller = new TaskFunctionSync(application.getRepository());
                            new TaskExecutor().execute(caller);
                        }
                    })
                    .setNegativeButton(R.string.confirm_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            AlertDialog alert = builder.create();
            alert.show();

        }
        else if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }
}
