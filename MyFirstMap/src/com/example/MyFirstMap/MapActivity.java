package com.example.MyFirstMap;

import android.app.Activity;
import android.content.Context;
import android.location.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends Activity implements LocationListener{

    private String mGPSProvider;
    private String mNetworkProvider;
    private static final String TAG = "MapActivity";

    private static final LatLng AUSTRALIA = new LatLng(-25, 135);
    private LocationManager mLocationManager;
    private boolean mIsReverseGeocoding = false;
    private GoogleMap map;
    private Marker marker;
    private boolean mFollow = true;

    private TextView mLocationTextView;
    private TextView mAddressTextView;
    private Button mButtonFollow;

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGPSProvider = LocationManager.GPS_PROVIDER;
        mNetworkProvider = LocationManager.NETWORK_PROVIDER;

        mLocationTextView = (TextView)findViewById(R.id.location);
        mAddressTextView = (TextView)findViewById(R.id.address);
        mButtonFollow = (Button)findViewById(R.id.button_follow);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if(map != null){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(AUSTRALIA, 3));
            map.setMyLocationEnabled(true);
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        if(checkLocationAccess()){
            Location location = mLocationManager.getLastKnownLocation(mGPSProvider);
            updateMarkerAndLocationAddress(location);
        };
    }

    public void followMyUserLocation(View view){
        Log.i(TAG, "followMyUserLocation Pressed");
        if(mFollow){
            mFollow = false;
            mButtonFollow.setText("Follow");
        }
        else{
            mFollow = true;
            mButtonFollow.setText("Unfollow");
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        if(checkLocationAccess()){
            Location location = mLocationManager.getLastKnownLocation(mGPSProvider);
            updateMarkerAndLocationAddress(location);

            mLocationManager.requestLocationUpdates(mGPSProvider, 0, 0, this);
        };
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        updateMarkerAndLocationAddress(location);
    }

    private boolean checkLocationAccess() {
        boolean gps_enabled = mLocationManager.isProviderEnabled(mNetworkProvider);
        boolean network_enabled = mLocationManager.isProviderEnabled(mNetworkProvider);

        if(gps_enabled && network_enabled){
            if(!map.isMyLocationEnabled()){
                map.setMyLocationEnabled(true);
            }

            return true;
        }
        else{
            mLocationTextView.setText("Enable Location Access");
            map.setMyLocationEnabled(false);

            return false;
        }
    }
    private void updateMarkerAndLocationAddress(Location location) {
        updateLocationAddressText(location);
        updateMarker(location);
    }

    private void updateMarker(Location location) {
        if(map != null){
            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

            if(marker == null){
                MarkerOptions markerOptions = new MarkerOptions().position(pos);
                marker = map.addMarker(markerOptions);
            }
            else{
                marker.setPosition(pos);
            }

            if(mFollow){
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 20));
            }
        }
    }

    private void updateLocationAddressText(Location location){
        if(location == null){
            mLocationTextView.setText("Null Location");
            return;
        }
        else{
            StringBuilder sb = new StringBuilder();
            sb.append(location.getProvider())
                    .append(": ")
                    .append(location.getLatitude())
                    .append(',')
                    .append(location.getLongitude())
                    .append(" (")
                    .append(location.getAccuracy())
                    .append(" )");

            mLocationTextView.setText(sb.toString());
        }

        if(!mIsReverseGeocoding){
            mIsReverseGeocoding = true;
            new ReverseGeocoderTask().execute(location);
        }
    }

    private class ReverseGeocoderTask extends AsyncTask<Location, Void, String> {

        @Override
        protected String doInBackground(Location... locations) {
            Location location = locations[0];
            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addresses != null && !addresses.isEmpty()){
                Address address = addresses.get(0);

                // Format the first line of address (if available), city, and country name.
                String addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());

                return addressText;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(TextUtils.isEmpty(result)){
                mAddressTextView.setText("Could not reverse geocode");
            }
            else{
                mAddressTextView.setText(result);
            }

            mIsReverseGeocoding = false;
        }

    }
}
