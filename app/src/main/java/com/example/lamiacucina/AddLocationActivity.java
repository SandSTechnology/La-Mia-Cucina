package com.example.lamiacucina;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lamiacucina.model.Store;
import com.example.lamiacucina.util.BaseUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "ShowLocationNotification";
    // as Google documentation
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    LocationCallback mLocationCallback;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    //int maxId;
    Boolean running, isFirstTimeAlert = true;
    Boolean isCalled = false, refreshRequest = true;
    FirebaseDatabase firebaseDatabase;
    ValueEventListener storeList_valueEventListener = null;
    Location location;
    double userLat;
    double userLong;
    EditText storeName;
    String userStoreName;
    String MyName = "user";
    AlertDialog alertDialog;
    Spinner rangeSpinner;
    ProgressBar ShowOnLoadingLocation;
    ValueEventListener myVal = null;
    private Boolean isFirstTimeZoom = true;
    private ArrayList<Store> storesList;
    private DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    int count = 0;
    boolean isCountingDone = false;
    //private View loadingView;
    private SweetAlertDialog pDialog;

    @Override
    public void finish() {
        if (storeList_valueEventListener != null)
            databaseReference.removeEventListener(storeList_valueEventListener);
        super.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        TextView btnSubmit = findViewById(R.id.btnSubmit);
        storeName = findViewById(R.id.storeName);
        btnSubmit.setOnClickListener(v -> {
            userStoreName = storeName.getText().toString().trim();

            if (userStoreName.equalsIgnoreCase("")) {
                Toast.makeText(this, "Please add Store name first", Toast.LENGTH_SHORT).show();
                storeName.requestFocus();
                storeName.setError("Add Store Name");
                return;
            }

            SaveRoom();
        });

        //loadingView=findViewById(R.id.loadingView);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading ...");
        pDialog.setCancelable(true);

        ShowOnLoadingLocation = findViewById(R.id.loadingOnLocation);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    location = locationList.get(locationList.size() - 1);
                    mLastLocation = location;
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }

                    addYouOnMap(location);
                    if (!isCalled || refreshRequest)
                        getStores();
                }
            }
        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        FragmentManager fm = getSupportFragmentManager();
        mapFrag = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (mapFrag == null) {
            mapFrag = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.map, mapFrag, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFrag.getMapAsync(this);
    }

    private void getStores() {
        if (!isCalled || refreshRequest) {
            refreshRequest = false;
            databaseReference.child("AppUsers").child("Seeker").addValueEventListener(storeList_valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        isCalled = true;
                        int SearchingRange = 0;

                        Double Latitude, Longitude;

                        if (rangeSpinner.getSelectedItemPosition() == 0) {
                            SearchingRange = 5;
                        } else if (rangeSpinner.getSelectedItemPosition() == 1) {
                            SearchingRange = 15;
                        } else if (rangeSpinner.getSelectedItemPosition() == 2) {
                            SearchingRange = 25;
                        } else if (rangeSpinner.getSelectedItemPosition() == 3) {
                            SearchingRange = 50;
                        }

                        if (storesList != null)
                            storesList.clear();
                        if (mGoogleMap != null)
                            mGoogleMap.clear();
                        storesList = new ArrayList<>();
                        for (DataSnapshot eachUserRecord : dataSnapshot.getChildren()) {

                            Latitude = null;
                            Longitude = null;

                            if (eachUserRecord.hasChild("Latitude")) {
                                Latitude = (Double) Objects.requireNonNull(eachUserRecord.child("Latitude").getValue());
                            }

                            if (eachUserRecord.hasChild("Longitude")) {
                                Longitude = (Double) Objects.requireNonNull(eachUserRecord.child("Longitude").getValue());
                            }

                            SharedPreferences sharedPreferences = getSharedPreferences("Categories", Context.MODE_PRIVATE);

                            if (Latitude == null || Longitude == null)
                                continue;

                            float[] results = new float[1];
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                    Latitude, Longitude, results);

                            if (!(results[0] / 1000 <= SearchingRange)) {
                                continue;
                            }

                            {
                                Store p = new Store();

                                /*p.setName(eachUserRecord.child("Name").getValue(String.class));
                                p.setId(eachUserRecord.child("Id").getValue(String.class));
                                p.setLatitude(Latitude);
                                p.setLongitude(Longitude);
                                p.setOrderCost(String.valueOf(results[0] / 1000));

                                if (eachUserRecord.hasChild("urlToImage")) {
                                    p.setUrl(eachUserRecord.child("urlToImage").getValue(String.class));
                                } else {
                                    p.setImage(R.drawable.profile);
                                }*/
                                storesList.add(p);
                            }
                        }

                        if (!storesList.isEmpty()) {
                            for (Store p : storesList) {

                            }
                            mGoogleMap.setOnMarkerClickListener(m -> {
                                if (Objects.requireNonNull(m.getTag()).toString().equals("CurrentUserLocation")) {

                                    return true;
                                }

                                for (Store p : storesList) {

                                }

                                return true;
                            });

                            if (pDialog.isShowing())
                                pDialog.dismiss();

                        } else {
                            if (pDialog.isShowing())
                                pDialog.dismiss();
                            if (isFirstTimeAlert) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddLocationActivity.this)
                                        .setCancelable(true)
                                        .setTitle("Message")
                                        .setMessage("No Available Stores in your Area")
                                        .setPositiveButton("OK", (dialog, which) -> dialog.cancel());

                                AlertDialog alertDialog = builder.create();

                                if (!((AddLocationActivity.this)).isFinishing()) {
                                    alertDialog.show();
                                }
                                isFirstTimeAlert = false;
                            }
                        }
                    } else {
                        if (pDialog.isShowing())
                            pDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (pDialog.isShowing())
                        pDialog.dismiss();
                }
            });
        }
    }

    private void addYouOnMap(Location location) {
        //Place current location marker

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        mCurrLocationMarker.setTag("CurrentUserLocation");
        mCurrLocationMarker.setDraggable(true);

        userLat = mCurrLocationMarker.getPosition().latitude;
        userLong = mCurrLocationMarker.getPosition().longitude;

        //move map camera
        if (isFirstTimeZoom) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            isFirstTimeZoom = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("System out", "onMarkerDragStart..." + arg0.getPosition().latitude + "..." + arg0.getPosition().longitude);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("System out", "onMarkerDragEnd..." + arg0.getPosition().latitude + "..." + arg0.getPosition().longitude);

                userLat = arg0.getPosition().latitude;
                userLong = arg0.getPosition().longitude;

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                Log.i("System out", "onMarkerDrag...");
            }
        });

        refresh();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(AddLocationActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mGoogleMap.setMyLocationEnabled(true);
                }

            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void SaveRoom() {
        DatabaseReference newRef = databaseReference.child("Stores").child((++count) + "");

        newRef.child("ID").setValue(count);
        newRef.child("NAME").setValue(userStoreName);
        newRef.child("LAT").setValue(userLat);
        newRef.child("LONG").setValue(userLong);
        newRef.child("FAMILY_ID").setValue(new BaseUtil(this).getFamilyID());

        Toast.makeText(this, "Store Added", Toast.LENGTH_SHORT).show();

        storeName.setText("");
    }


    private void refresh() {
        pDialog.show();

        isFirstTimeAlert = true;
        mLocationRequest = LocationRequest.create();
        //mLocationRequest.setInterval(10000);
        //mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }
}