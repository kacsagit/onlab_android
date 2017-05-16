package com.example.kata.onlab.ui.main;


import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kata.onlab.R;
import com.example.kata.onlab.db.Data;
import com.example.kata.onlab.db.Friends;
import com.example.kata.onlab.db.MyData;
import com.example.kata.onlab.network.NetApi;
import com.example.kata.onlab.network.NetworkManager;
import com.example.kata.onlab.ui.friends.FriendsActivity;
import com.example.kata.onlab.ui.friendsfragment.FriendsFragment;
import com.example.kata.onlab.ui.friendsfragment.OnMenuSelectionSetListener;
import com.example.kata.onlab.ui.login.LoginActivity;
import com.example.kata.onlab.service.GeofenceTransitionsIntentService;
import com.example.kata.onlab.ui.map.MapFragment;
import com.example.kata.onlab.service.ServiceLocation;
import com.example.kata.onlab.ui.myList.MyListFragment;
import com.example.kata.onlab.ui.myMap.MyMapFragment;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements MainScreen, NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener, OnMenuSelectionSetListener, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "MainActivity";
    public static final String KEY_START_SERVICE = "start_service";
    SwitchCompat switchCompat;
    SharedPreferences preferences;
    Fragment fragment;
    TextView user;
    CircularImageView image;
    private static final int LIST = R.id.action_list;
    private static final int MAP = R.id.action_map;
    private static final int FRIENDS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("My todos");
        mGeofenceListMy=new ArrayList<>();
        mGeofenceList=new ArrayList<>();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View header = navigationView.getHeaderView(0);

        user = (TextView) header.findViewById(R.id.textView);

        image = (CircularImageView) header.findViewById(R.id.imageView);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        MenuItem menu = navigationView.getMenu().findItem(R.id.loc_monitor);
        LinearLayout i = (LinearLayout) menu.getActionView();
        switchCompat = (SwitchCompat) i.findViewById(R.id.toggleButton);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_START_SERVICE, isChecked);
                editor.apply();
            }
        });
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            int frag = savedInstanceState.getInt("curChoice", FRIENDS);
            fragmentSelect(frag);
        } else {
            fragmentSelect(FRIENDS);
        }

        buildGoogleApiClient();
        if (preferences.getBoolean(KEY_START_SERVICE, true)) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this,
                            "I need it for gps", Toast.LENGTH_SHORT).show();
                }

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        101);
            } else {


                switchCompat.setChecked(true);
                Intent intetn2 = new Intent(this, ServiceLocation.class);
                startService(intetn2);
                LocalBroadcastManager.getInstance(this).registerReceiver(
                        mMessageReceiver,
                        new IntentFilter(ServiceLocation.BR_NEW_LOCATION));
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        NetworkManager.getInstance().getme();
        NetworkManager.getInstance().updateData();
        NetworkManager.getInstance().updateDataMy();


    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location currentLocation = intent.getParcelableExtra(ServiceLocation.KEY_LOCATION);

        }
    };
    GoogleApiClient mGoogleApiClient;

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(connectionAddListener)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private GoogleApiClient.ConnectionCallbacks connectionAddListener =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    try {
                        if ((mGeofenceList != null && !mGeofenceList.isEmpty())|| (mGeofenceListMy != null && !mGeofenceListMy.isEmpty())) {
                            LocationServices.GeofencingApi.addGeofences(
                                    mGoogleApiClient,
                                    getGeofencingRequest(),
                                    getGeofencePendingIntent()
                            ).setResultCallback(new ResultCallback<Status>() {

                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                        Log.i(TAG, "Saving Geofence");

                                    } else {
                                        Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                                                " : " + status.getStatusCode());
                                    }
                                }
                            });
                        }
                    } catch (SecurityException securityException) {
                        // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                        Log.e(TAG, "Error");
                    }

                }

                @Override
                public void onConnectionSuspended(int i) {

                    Log.e(TAG, "onConnectionSuspended");

                }
            };

    private List<Geofence> mGeofenceListMy;
    private List<Geofence> mGeofenceList;

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        builder.addGeofences(mGeofenceList);
        builder.addGeofences(mGeofenceListMy);

        return builder.build();


    }

    public void createGeofences(List<Data> datalist) {
        for (Data d : datalist) {
            String id = UUID.randomUUID().toString();
            Geofence fence = new Geofence.Builder()
                    .setRequestId(id)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setCircularRegion(d.getLatitude(), d.getLongitude(), 3000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build();
            mGeofenceList.add(fence);
        }
    }

    public void createGeofencesMy(List<MyData> datalist) {
        for (MyData d : datalist) {
            String id = UUID.randomUUID().toString();
            Geofence fence = new Geofence.Builder()
                    .setRequestId(id)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setCircularRegion(d.getLatitude(), d.getLongitude(), 3000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build();
            mGeofenceListMy.add(fence);
        }
    }

    PendingIntent mGeofencePendingIntent;

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);


        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }


    @Override
    protected void onStart() {
        super.onStart();
        MainPresenter.getInstance().attachScreen(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainPresenter.getInstance().detachScreen();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

     /*   //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Token", "");
            editor.putString("Email", "");
            editor.apply();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        String token = FirebaseInstanceId.getInstance().getToken();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            LoginManager.getInstance().logOut();
        } else if (id == R.id.nav_mytodo) {
            fragmentSelect(LIST);
        } else if (id == R.id.nav_friendtodo) {
            fragmentSelect(FRIENDS);


        } else if (id == R.id.nav_friends) {
            Intent intent = new Intent(this, FriendsActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_manage) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this,
                            "I need it for picking an image", Toast.LENGTH_SHORT).show();
                }

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        101);
            } else {
                Crop.pickImage(this);

               /* Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);*/
            }

        }/*  else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/
        if (id == R.id.loc_monitor) {

            switchCompat.toggle();
            return true;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    NetworkManager.getInstance().postImage(getRealPathFromURI(selectedImage));
                }
                break;
            case Crop.REQUEST_PICK:
                if (resultCode == RESULT_OK) {
                    beginCrop(imageReturnedIntent.getData());
                }
            case Crop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    handleCrop(resultCode, imageReturnedIntent);
                    break;
                }
        }
    }


    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped.jpg"));
       /* Uri destination =FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider",new File(getCacheDir(), "cropped"));*/
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            NetworkManager.getInstance().postImage(getRealPathFromURI(Crop.getOutput(result)));


        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result = "";
        try {
            Cursor cursor = this.getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx); // Exception raised HERE
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_START_SERVICE.equals(key)) {
            boolean startService = sharedPreferences.getBoolean(KEY_START_SERVICE, false);
            Intent i = new Intent(getApplicationContext(), ServiceLocation.class);
            if (startService) {
                startService(i);
                LocalBroadcastManager.getInstance(this).registerReceiver(
                        mMessageReceiver,
                        new IntentFilter(ServiceLocation.BR_NEW_LOCATION));
            } else {
                stopService(i);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            }
        }
    }

    @Override
    public void updateProfile(Friends data) {
        user.setText(data.name);
        String url = NetApi.GETIMEAGE + data.image;
        Picasso.with(this).load(url).placeholder(R.drawable.avatar).into(image);

    }

    @Override
    public void updateDataCallback(List<Data> data) {
        if (data != null) {
            if (mGeofenceList != null)
                mGeofenceList.clear();
            createGeofences(data);
        }
    }


    @Override
    public void updateDataCallbackMy(List<MyData> data) {
        if (data != null) {
            if (mGeofenceListMy != null)
                mGeofenceListMy.clear();
            createGeofencesMy(data);
        }
    }

    @Override
    public void photoUploaded(String data) {
        String url = NetApi.GETIMEAGE + data;
        Picasso.with(this).load(url).placeholder(R.drawable.avatar).into(image);
    }


    public void fragmentSelect(int id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (id) {
            case MAP:
                fragment = new MyMapFragment();
                fragmentTransaction.replace(R.id.content_frame, fragment, "MAP");
                break;

            case LIST:
                fragment = new MyListFragment();
                fragmentTransaction.replace(R.id.content_frame, fragment, "LIST");
                break;
            case FRIENDS:
                fragment = new FriendsFragment();
                Fragment f = fragmentManager.findFragmentByTag("FRIEND");
                if (f == null) {
                    fragmentTransaction.replace(R.id.content_frame, fragment, "FRIEND");
                }
                break;

        }
        fragmentTransaction.commit();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MyListFragment myFragmentlist = (MyListFragment) getSupportFragmentManager().findFragmentByTag("LIST");
        if (myFragmentlist != null && myFragmentlist.isVisible()) {
            outState.putInt("curChoice", LIST);
        } else {
            MapFragment myFragmentmap = (MapFragment) getSupportFragmentManager().findFragmentByTag("MAP");
            if (myFragmentmap != null && myFragmentmap.isVisible()) {
                outState.putInt("curChoice", MAP);
            } else {
                FriendsFragment myFragment = (FriendsFragment) getSupportFragmentManager().findFragmentByTag("LIST");
                if (myFragment != null && myFragment.isVisible()) {
                    outState.putInt("curChoice", FRIENDS);
                }
            }
        }

    }

    @Override
    public void onPlayerSelectionSet(int id) {
        fragmentSelect(id);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "GEO API FAILED: " +
                connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
}
