package com.example.MyFirstMap;

import android.app.Activity;
import android.content.Context;
import android.location.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends Activity implements LocationListener{

    private LocationManager mLocationManager;
    private String mProvider;
    private TextView mLocationTextView;
    private TextView mAddressTextView;
    private boolean mIsReverseGeocoding = false;

    private GoogleMap map;

    @Override
    public void onLocationChanged(Location location) {
        updateLocationView(location);
    }

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

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mProvider = LocationManager.GPS_PROVIDER;
        mLocationTextView = (TextView)findViewById(R.id.location);
        mAddressTextView = (TextView)findViewById(R.id.address);
        Location location = mLocationManager.getLastKnownLocation(mProvider);
        updateLocationView(location);
    }

    private void updateLocationView(Location location){
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

    @Override
    protected void onResume() {
        super.onResume();
        if(mLocationManager.isProviderEnabled(mProvider)){
            mLocationManager.requestLocationUpdates(mProvider, 0, 0, this);
        }
        else{
            mLocationTextView.setText("LocationProvider disabled");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
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
                return address.getLocality();
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
