package com.example.lamiacucina;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class GoogleLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    // as Google documentation
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    LocationCallback mLocationCallback;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    //int maxId;
    Boolean isFirstTimeAlert = true;
    Boolean isCalled = false, refreshRequest = true;
    FirebaseDatabase firebaseDatabase;
    ValueEventListener storeList_valueEventListener = null;
    Location location;
    AlertDialog alertDialog;
    Spinner rangeSpinner;
    ProgressBar ShowOnLoadingLocation;
    private Boolean isFirstTimeZoom = true;
    private ArrayList<Store> storesList;
    private DatabaseReference databaseReference;
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
        setContentView(R.layout.activity_google_location);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading ...");
        pDialog.setCancelable(true);

        ShowOnLoadingLocation = findViewById(R.id.loadingOnLocation);
        TextView refresh = findViewById(R.id.refreshProcess);
        FloatingActionButton floatAction = findViewById(R.id.floatingBtn);
        refresh.setOnClickListener(v -> {
            refreshRequest = true;
            refresh();
        });

        //get the spinner from the xml.
        rangeSpinner = findViewById(R.id.spinner_range);
        String[] items = new String[]{"5km", "15km", "25km", "50km"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        rangeSpinner.setAdapter(adapter);

        floatAction.setOnClickListener(view -> startActivity(new Intent(GoogleLocationActivity.this, AddLocationActivity.class)));

        rangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshRequest = true;
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        String FamilyID = new BaseUtil(this).getFamilyID();
        if (!isCalled || refreshRequest) {
            refreshRequest = false;
            databaseReference.child("Stores").addValueEventListener(storeList_valueEventListener = new ValueEventListener() {
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

                        //Getting All Stores
                        for (DataSnapshot eachUserRecord : dataSnapshot.getChildren()) {

                            String mFamilyID = eachUserRecord.child("FAMILY_ID").getValue(String.class);
                            if (Objects.equals(mFamilyID, FamilyID))
                            {
                                Latitude = null;
                                Longitude = null;

                                if (eachUserRecord.hasChild("LAT")) {
                                    Latitude = (Double) Objects.requireNonNull(eachUserRecord.child("LAT").getValue());
                                }

                                if (eachUserRecord.hasChild("LONG")) {
                                    Longitude = (Double) Objects.requireNonNull(eachUserRecord.child("LONG").getValue());
                                }

                                if (Latitude == null || Longitude == null)
                                    continue;

                                float[] results = new float[1];
                                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                        Latitude, Longitude, results);

                                if (!(results[0] / 1000 <= SearchingRange)) {
                                    continue;
                                }

                                Store p = new Store();

                                if (eachUserRecord.hasChild("ID"))
                                    p.setID(String.valueOf(eachUserRecord.child("ID").getValue()));
                                if (eachUserRecord.hasChild("NAME"))
                                    p.setNAME(eachUserRecord.child("NAME").getValue(String.class));
                                p.setLAT(Latitude.toString());
                                p.setLONG(Longitude.toString());

                                storesList.add(p);
                            }
                        }

                        if (!storesList.isEmpty()) {
                            for (Store p : storesList) {
                                //Place current location marker
                                LatLng latLng = new LatLng(Double.parseDouble(p.getLAT()), Double.parseDouble(p.getLONG()));
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(p.getNAME());
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                Marker marker = mGoogleMap.addMarker(markerOptions);
                                assert marker != null;
                                marker.setTag(p.getID());
                            }
                            mGoogleMap.setOnMarkerClickListener(m -> {
                                if (Objects.requireNonNull(m.getTag()).toString().equals("CurrentUserLocation")) {
                                    new AlertDialog.Builder(GoogleLocationActivity.this)
                                            .setCancelable(true)
                                            .setTitle("Your Location")
                                            .setMessage("This is You on Map")
                                            .setPositiveButton("OK", (dialog, which) -> dialog.cancel()).create().show();
                                    return true;
                                }

                                for (Store p : storesList) {
                                    if (p.getID().equals(Objects.requireNonNull(m.getTag()).toString())) {
                                        TextView StoreNameTextView;
                                        TextView DeleteBtn;

                                        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleLocationActivity.this);
                                        ViewGroup viewGroup = findViewById(android.R.id.content);
                                        View dialogView = LayoutInflater.from(GoogleLocationActivity.this).inflate(R.layout.store_dialog, viewGroup, false);
                                        builder.setView(dialogView);
                                        alertDialog = builder.create();

                                        StoreNameTextView = dialogView.findViewById(R.id.StoreName);
                                        DeleteBtn = dialogView.findViewById(R.id.DeleteBtn);

                                        DeleteBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                databaseReference.child("Stores").child(p.getID()).removeValue();
                                                Toast.makeText(GoogleLocationActivity.this, "Store Deleted", Toast.LENGTH_SHORT).show();
                                                storesList.remove(p);
                                                m.remove();
                                                alertDialog.dismiss();
                                            }
                                        });

                                        StoreNameTextView.setText(p.getNAME());

                                        alertDialog.show();
                                    }
                                }
                                return true;
                            });

                            if (pDialog.isShowing())
                                pDialog.dismiss();

                        } else {
                            if (pDialog.isShowing())
                                pDialog.dismiss();
                            if (isFirstTimeAlert) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(GoogleLocationActivity.this)
                                        .setCancelable(true)
                                        .setTitle("Message")
                                        .setMessage("No Available Store in your Area")
                                        .setPositiveButton("OK", (dialog, which) -> dialog.cancel());

                                AlertDialog alertDialog = builder.create();

                                if (!((GoogleLocationActivity.this)).isFinishing()) {
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
        assert mCurrLocationMarker != null;
        mCurrLocationMarker.setTag("CurrentUserLocation");

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
                            ActivityCompat.requestPermissions(GoogleLocationActivity.this,
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

    private void refresh() {
        pDialog.show();

        isFirstTimeAlert = true;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
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