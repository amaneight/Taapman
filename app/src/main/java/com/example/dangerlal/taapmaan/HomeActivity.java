package com.example.dangerlal.taapmaan;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dangerlal.taapmaan.APIClient.WeatherClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    TextView txtTemperature;
    TextView txtLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtLocation = (TextView) findViewById(R.id.tv_location);
        txtTemperature = (TextView) findViewById(R.id.tv_temperature);

        // Set permissions
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
        locationTrack = new LocationTrack(HomeActivity.this);
        FetchCoordinates();
    }

    //Fetch un asked permssions
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    //Check if permission has been granted or not
    private boolean hasPermission(String permission) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == ALL_PERMISSIONS_RESULT){
            for (String perms : permissionsToRequest) {
                if (!hasPermission(perms)) {
                    permissionsRejected.add(perms);
                }
            }
        }

        if (permissionsRejected.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    showMessageOKCancel("Please provide these permissions.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                    }
                                }
                            });
                    return;
                }
            }
        }else {
            FetchCoordinates();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(HomeActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Get the cooridnates
    public List<Double> FetchCoordinates(){
        List<Double> coordinates = new ArrayList<>();
        locationTrack = new LocationTrack(HomeActivity.this);

        if (locationTrack.canGetLocation()) {
            coordinates.add(0,locationTrack.FetchLatitude());
            coordinates.add(1,locationTrack.FetchLongitude());

        }

        FetchTemperature fetchTemperature = new FetchTemperature(String.valueOf(locationTrack.FetchLatitude()),String.valueOf(locationTrack.FetchLongitude()));
        fetchTemperature.execute((Void)null);
        return coordinates;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.StopListener();
    }

    // Asynchronous operation to fetch Temperature from API
    public class FetchTemperature extends AsyncTask<Void, Void, JSONObject> {

        String lat;
        String lon;

        public FetchTemperature(String lat, String lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            // Fetch temperature from weather client
            WeatherClient wcObj = new WeatherClient();
            return wcObj.fetchCurrentTemperature(this.getLat(),this.getLon());
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                // Format the temperature
                String temperature = new DecimalFormat("#.0#####", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(jsonObject.getDouble("strTemperature"));
                txtTemperature.setText(temperature+"ºC");
                txtLocation.setText(jsonObject.getString("strLocation"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
