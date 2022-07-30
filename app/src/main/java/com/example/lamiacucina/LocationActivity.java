package com.example.lamiacucina;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.Objects;

public class LocationActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 555;
    int ACCESS_FINE_LOCATION_CODE = 3310;
    int ACCESS_COARSE_LOCATION_CODE = 3410;
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Build Google API Client for Location related work
        buildGoogleApiClient();
    }


    // When user first come to this activity we try to connect Google services for location and map related work
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    // Google Api Client is connected
    @Override
    public void onConnected(Bundle bundle) {
        if (mGoogleApiClient.isConnected()) {
            //if connected successfully show user the settings dialog to enable location from settings services
            // If location services are enabled then get Location directly
            // Else show options for enable or disable location services
            settingsrequest();
        }
    }

    // This is the method that will be called if user has disabled the location services in the device settings
    // This will show a dialog asking user to enable location services or not
    // If user tap on "Yes" it will directly enable the services without taking user to the device settings
    // If user tap "No" it will just Finish the current Activity
    public void settingsrequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    if (mGoogleApiClient.isConnected()) {
                        // check if the device has OS Marshmellow or greater than
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                            if (ActivityCompat.checkSelfPermission(LocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(LocationActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
                            } else {
                                startActivity(new Intent(LocationActivity.this, GoogleLocationActivity.class));
                                this.finish();
                                // get Location
                            }
                        } else {
                            startActivity(new Intent(LocationActivity.this, GoogleLocationActivity.class));
                            this.finish();
                            // get Location
                        }
                    }
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(LocationActivity.this, REQUEST_RESOLVE_ERROR);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    break;
            }
        });
    }

    // This method is called only on devices having installed Android version >= M (Marshmellow)
    // This method is just to show the user options for allow or deny location services at runtime
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3310) {
            if (grantResults.length > 0) {

                for (int i = 0, len = permissions.length; i < len; i++) {

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // Show the user a dialog why you need location
                        Toast.makeText(this, "Please allow Location to Check your nearest Services", Toast.LENGTH_LONG).show();
                    } else if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        startActivity(new Intent(LocationActivity.this, GoogleLocationActivity.class));
                        this.finish();
                        // get Location
                    } else {
                        this.finish();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            switch (resultCode) {
                case Activity.RESULT_OK:
                    startActivity(new Intent(LocationActivity.this, GoogleLocationActivity.class));
                    this.finish();
                    // get location method
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "Please allow Location to Check your nearest Services", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
    }


    // When there is an error connecting Google Services
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }


    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    // Connect Google Api Client if it is not connected already
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    // Stop the service when we are leaving this activity
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            assert this.getArguments() != null;
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return Objects.requireNonNull(GoogleApiAvailability.getInstance().getErrorDialog(
                    this.requireActivity(), errorCode, REQUEST_RESOLVE_ERROR));
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            ((LocationActivity) requireActivity()).onDialogDismissed();
        }
    }
}