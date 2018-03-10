package org.netzd.googlemaps;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationSource, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 1266;
    public static final int LOCALIZATION_PERMISSION = 1267;
    public static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    private GoogleMap googleMap = null;
    private SupportMapFragment mapFragment = null;

    private GoogleApiClient googleApiClient = null;
    private LocationRequest locationRequest = null;

    private LatLng ubicacionActual = null;

    private LatLng ubicacionDefault = new LatLng(19.434552, -99.149700);

    public static final int ZOOM_DEFAUlT = 14;
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(checkPlayServices()){
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean checkPlayServices() {
        int status = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(status)) {
                mostrarErrorPlayServices(status);
            } else {
                Toast.makeText(getBaseContext(),
                        "Este dispositivo no soporta los servicios de Google Play",
                        Toast.LENGTH_LONG).show();
            }
            return false;
        } else {
            return true;
        }
    }

    private void mostrarErrorPlayServices(int status) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        apiAvailability.getErrorDialog(this,
                status, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    private boolean verificarPermisos() {
        boolean hasPermissionCoarseLocation = (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean hasPermissionFineLocation = (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean hasPermissionWrite = (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermissionCoarseLocation && !hasPermissionFineLocation && !hasPermissionWrite) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.
                    shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                DialogWarning dialogWarning = DialogWarning.newInstance("Aviso",
                        "Debe autorizar los permisos para " +
                                "utilizar el GPS");
                dialogWarning.setOnDialogWarningListener(new OnDialogWarningListener() {
                    @Override
                    public void onAccept(Dialog dialog) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCALIZATION_PERMISSION);
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancel(Dialog dialog) {
                        dialog.dismiss();
                    }
                });
                dialogWarning.show(getSupportFragmentManager(), "dialogo");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        LOCALIZATION_PERMISSION);
            }
        }


        if (!hasPermissionCoarseLocation && !hasPermissionFineLocation) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.
                    shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                DialogWarning dialogWarning = DialogWarning.newInstance("Aviso",
                        "Debe autorizar los permisos para " +
                                "utilizar el GPS");
                dialogWarning.setOnDialogWarningListener(new OnDialogWarningListener() {
                    @Override
                    public void onAccept(Dialog dialog) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCALIZATION_PERMISSION);
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancel(Dialog dialog) {
                        dialog.dismiss();
                    }
                });
                dialogWarning.show(getSupportFragmentManager(), "dialogo");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCALIZATION_PERMISSION);
            }
        }
        if (!hasPermissionWrite) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                DialogWarning dialogWarning = DialogWarning.newInstance("Aviso",
                        "Debe autorizar los permisos para " +
                                "utilizar el GPS");
                dialogWarning.setOnDialogWarningListener(new OnDialogWarningListener() {
                    @Override
                    public void onAccept(Dialog dialog) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_STORAGE_PERMISSION);
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancel(Dialog dialog) {
                        dialog.dismiss();
                    }
                });
                dialogWarning.show(getSupportFragmentManager(), "dialogo");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_PERMISSION);
            }
        }

        return hasPermissionCoarseLocation && hasPermissionFineLocation && hasPermissionWrite;
    }

    public boolean isEnabledGPS(){
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return false;
        else
            return true;
    }

    public void detenerLocalizacion () {
        LocationServices.getFusedLocationProviderClient(getApplicationContext())
                .removeLocationUpdates(new LocationCallback() {
                    @Override
                    public void onLocationResult (LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                });
    }



    //Se ejecuta cuando el mapa esta listo
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
        try{
            this.googleMap.setMyLocationEnabled(true);
            this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            this.googleMap.getUiSettings().setZoomControlsEnabled(false);
            this.googleMap.getUiSettings().setZoomGesturesEnabled(true);
            this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        }catch (Exception e){

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mostrarUbicacionActual(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        detenerLocalizacion();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(isEnabledGPS()){
            if(verificarPermisos()){
                locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                builder.addLocationRequest(locationRequest);
                LocationSettingsRequest locationSettingsRequest = builder.build();
                SettingsClient settingsClient = LocationServices.getSettingsClient(getApplicationContext());
                settingsClient.checkLocationSettings(locationSettingsRequest);
                LocationServices.getFusedLocationProviderClient(getApplicationContext()).requestLocationUpdates(locationRequest, new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                }, getMainLooper().myLooper());
            }
        }
    }


    private void mostrarUbicacionActual (Location ubicacionActualLocation) {

        if (ubicacionActual == null) {
            ubicacionActual = ubicacionDefault;
        } else {
            ubicacionActual = new LatLng(ubicacionActualLocation.getLatitude(), ubicacionActualLocation.getLongitude());
        }
        if (ubicacionActual != null) {
            CameraPosition position = new CameraPosition.Builder().target(ubicacionActual)
                    .zoom(ZOOM_DEFAUlT).bearing(0).tilt(0).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
            googleMap.animateCamera(cameraUpdate);
        } else {
            CameraPosition position = new CameraPosition.Builder().target(ubicacionDefault)
                    .zoom(ZOOM_DEFAUlT).bearing(0).tilt(0).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
            googleMap.animateCamera(cameraUpdate);
        }
        CameraPosition position = new CameraPosition.Builder().target(ubicacionActual).zoom(ZOOM_DEFAUlT).
                bearing(0).tilt(0).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
        googleMap.animateCamera(cameraUpdate);

        googleMap.addMarker(new MarkerOptions().position(ubicacionActual)
                .title("Tu ubicacion"));
    }



    @Override
    public void onConnectionSuspended(int i) {
        detenerLocalizacion();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}