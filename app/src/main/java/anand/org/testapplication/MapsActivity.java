package anand.org.testapplication;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    public GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;

    /* GPS Constant Permission */
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private static int DEFAULT_ZOOM = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000 * 10)
                .setFastestInterval(5 * 1000);
    }

    private void goToLocation(double lat, double lng, String locality) {
        if (mMap != null) {
            LatLng local = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(local).title(locality));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(local, DEFAULT_ZOOM));
            //Toast.makeText(this, "Map is available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Map is NOT available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        // goToLocation(22,77);
        /*LatLng india = new LatLng(22, 77);
        mMap.addMarker(new MarkerOptions().position(india).title("INDIA!!!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(india));*/
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void locateGeo(View v) throws IOException {
        hideSoftKeyboard(v);
        EditText et = (EditText) findViewById(R.id.inputLoc);
        String location = et.getText().toString();
        if(location.length() == 0){
            goToLocation(22, 77, "INDIA");
            return;
        }
        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);
        Address add = list.get(0);
        String locality = add.getLocality();
        Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();
        goToLocation(add.getLatitude(), add.getLongitude(), locality);
    }

    public void locateMe(View v) {
        // The ACCESS_COARSE_LOCATION is denied, then I request it and manage the result in
        // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
        // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }

        if(mGoogleApiClient == null){
            Toast.makeText(this, "GoogleApiClient is NULL !!! ", Toast.LENGTH_SHORT).show();
        }
        if(!mGoogleApiClient.isConnected()){
            Toast.makeText(this, "GoogleApiClient not connected !!! ", Toast.LENGTH_SHORT).show();
            mGoogleApiClient.connect();
        }
        //Toast.makeText(this, "ABout to get the last location !!! ", Toast.LENGTH_SHORT).show();
        Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(myLocation == null){
//            Toast.makeText(this, "Location not available !!! ", Toast.LENGTH_SHORT).show();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }else{
            Geocoder gc = new Geocoder(this);
            Address address = null;
            try {
                address = gc.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(),1).get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            goToLocation(myLocation.getLatitude(), myLocation.getLongitude(), address.getLocality());
//            Toast.makeText(this, "Location IS available !!! ", Toast.LENGTH_SHORT).show();
//            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),5);
//            mMap.animateCamera(update);
        }
    }

    private void hideSoftKeyboard(View v){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // The ACCESS_COARSE_LOCATION is denied, then I request it and manage the result in
        // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
        // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location == null){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        Geocoder gc = new Geocoder(this);
        Address address = null;
        try {
            address = gc.getFromLocation(location.getLatitude(), location.getLongitude(),1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        goToLocation(location.getLatitude(), location.getLongitude(),address.getLocality());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed !!! ", Toast.LENGTH_SHORT);
    }

    @Override
    public void onLocationChanged(Location location) {
        //goToLocation(location.getLatitude(), location.getLongitude());
        Toast.makeText(this, "Location : "+location.getLatitude() + ", " + location.getLongitude(),Toast.LENGTH_SHORT).show();
    }

}
