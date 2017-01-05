package com.example.swin.testwearapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener,
        DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private Activity activity;
    private GoogleApiClient googleClient;
    private GoogleApiClient mGoogleApiClient;
    Location currentLocation;
    Double currentLng;
    Double currentLat;
    Spinner spinner;
    Spinner spinner1;
    Spinner spinner2;
    TextView textView1;
    TextView textView2;
    ProgressDialog progressDialog;
    UrlCreator urlCreator;
    String selectedRouteId = null;
    String selectedRouteName = null;
    String selectedStopId1 = null;
    String selectedStopName1 = null;
    String selectedStopId2 = null;
    String selectedStopName2 = null;
    String stop1lng = null;
    String stop1lat = null;
    String stop2lng = null;
    String stop2lat = null;
    String direction_id=null;
    String direction = "";
    private LinkedHashMap<String, String> trainRoutes = new LinkedHashMap<>();
    private LinkedHashMap<String, String[]> trainStops;
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final int REQUEST_CODE_PERMISSION = 2;
    //String mPermission = Manifest.permission.
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.activity = this;
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        selectedRouteId = settings.getString("selectedRouteId", null);
        selectedRouteName = settings.getString("selectedRouteName", null);
        selectedStopId1 = settings.getString("selectedStopId1", null);
        selectedStopName1 = settings.getString("selectedStopName1", null);
        selectedStopId2 = settings.getString("selectedStopId2", null);
        selectedStopName2 = settings.getString("selectedStopName2", null);
        stop1lat = settings.getString("stop1lat", null);
        stop1lng = settings.getString("stop1lng", null);
        stop2lat = settings.getString("stop2lat", null);
        stop2lng = settings.getString("stop2lng", null);
        initializeUI();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();
    }

    public void initializeUI()
    {
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner1 = (Spinner) findViewById(R.id.spinner2);
        spinner2 = (Spinner) findViewById(R.id.spinner3);
        textView1 = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView3);
        if(this.selectedRouteName!=null && selectedStopName1!=null)
        {
            textView1.setText("Current Route: "+selectedRouteName);
            textView2.setText("Selected Stops:\n"+selectedStopName1+"\n"+selectedStopName2);
        }
        new UrlCreator().createUrl(R.string.request_routes, new String[0]);
        urlCreator = new UrlCreator();
        String[] url = {urlCreator.createUrl(R.string.request_routes, new String[0])};
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Routes from PTV database!");
        progressDialog.show();
        progressDialog.setCancelable(false);
        new AsyncApiRequest().execute(url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.save:
                selectedStopName1 = String.valueOf(spinner1.getSelectedItem());
                selectedStopId1 = trainStops.get(selectedStopName1)[0];
                selectedStopName2 = String.valueOf(spinner2.getSelectedItem());
                selectedStopId2 = trainStops.get(selectedStopName2)[0];
                stop1lat = trainStops.get(selectedStopName1)[1];
                stop1lng = trainStops.get(selectedStopName2)[2];
                stop2lat = trainStops.get(selectedStopName2)[1];
                stop2lng = trainStops.get(selectedStopName2)[2];
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("selectedRouteName", selectedRouteName);
                editor.putString("selectedRouteId", selectedRouteId);
                editor.putString("selectedStopName1", selectedStopName1);
                editor.putString("selectedStopId1", selectedStopId1);
                editor.putString("selectedStopName2", selectedStopName2);
                editor.putString("selectedStopId2", selectedStopId2);
                editor.putString("stop1lat", stop1lat);
                editor.putString("stop1lng", stop1lng);
                editor.putString("stop2lat", stop2lat);
                editor.putString("stop2lng", stop2lng);
                editor.commit();
                textView1.setText("Current Route: "+selectedRouteName);
                textView2.setText("Selected Stops:\n"+selectedStopName1+"\n"+selectedStopName2);
                urlCreator = new UrlCreator();
                String[] param = {selectedRouteId};
                String[] url = {urlCreator.createUrl(R.string.request_direction, param)};
                new AsyncApiRequest().execute(url);
                Toast.makeText(getApplicationContext(), "Your preferences are saved!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), com.example.swin.testwearapp.SettingsActivity.class));
                return true;
            case R.id.exit:
                Location stop1Location = new Location("");
                stop1Location.setLatitude(Double.parseDouble(stop1lat));
                stop1Location.setLongitude(Double.parseDouble(stop1lng));

                Location stop2Location = new Location("");
                stop2Location.setLatitude(Double.parseDouble(stop2lat));
                stop2Location.setLongitude(Double.parseDouble(stop2lng));

                Location currentLocation = new Location("");
                currentLocation.setLatitude(-37.822126);
                currentLocation.setLongitude(145.036793);

                Location cityLocation = new Location("");
                cityLocation.setLatitude(-37.8183327);
                cityLocation.setLongitude(144.95253);

                float s1DistToCity = stop1Location.distanceTo(cityLocation);
                float s2DistToCity = stop2Location.distanceTo(cityLocation);
                float s1DistToCurrentLoc = stop1Location.distanceTo(currentLocation);
                float s2DistToCurrentLoc = stop2Location.distanceTo(currentLocation);
                String direction_Id = "";

                if(s1DistToCurrentLoc > s2DistToCurrentLoc)
                {
                    if(s1DistToCity < s2DistToCity)
                    {
                        direction_Id= "0";
                        direction = "From "+selectedStopName1+" to "+selectedStopName2;
                    }
                    else if(s1DistToCity > s2DistToCity)
                    {
                        direction_Id = this.direction_id;
                        direction = "From "+selectedStopName2+" to "+selectedStopName1;
                    }
                }
                else if(s1DistToCurrentLoc > s2DistToCurrentLoc)
                {
                    if(s1DistToCity > s2DistToCity)
                    {
                        direction_Id= "0";
                        direction = "From "+selectedStopName1+" to "+selectedStopName2;
                    }
                    else if(s1DistToCity < s2DistToCity)
                    {
                        direction_Id = this.direction_id;
                        direction = "From "+selectedStopName2+" to "+selectedStopName1;
                    }
                }

                urlCreator = new UrlCreator();
                String[] param1 = {selectedRouteId, selectedStopId1, direction_Id};
                String[] url1 = {urlCreator.createUrl(R.string.request_departures, param1)};
                new AsyncApiRequest().execute(url1);
                return true;
            default:
                Log.i("onOptionsItemSelected", "default case - invalid value");
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void getDataFromResponse(String response)
    {
        try {
            JSONObject jsonObj = new JSONObject(response);
            switch (urlCreator.getRequest_type())
            {
                case R.string.request_routes:
                    JSONArray routes = jsonObj.getJSONArray("routes");
                    for(int i=0; i<routes.length(); i++)
                    {
                        JSONObject route = routes.getJSONObject(i);
                        trainRoutes.put(route.getString("route_name"), String.valueOf(route.getInt("route_id")));
                    }
                    String[] routeNames = new String[trainRoutes.size()];
                    routeNames = trainRoutes.keySet().toArray(routeNames);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, routeNames);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(dataAdapter);
                    spinner.setOnItemSelectedListener(this);
                    break;
                case R.string.request_stops:
                    JSONArray stops = jsonObj.getJSONArray("stops");
                    trainStops = new LinkedHashMap<>();
                    for(int i=0; i<stops.length(); i++)
                    {
                        JSONObject stop = stops.getJSONObject(i);
                        String ar[] = {String.valueOf(stop.getInt("stop_id")), String.valueOf(stop.getDouble("stop_latitude")), String.valueOf(stop.getDouble("stop_longitude"))};
                        trainStops.put(stop.getString("stop_name"), ar);
                    }
                    String[] stopNames = new String[trainStops.size()];
                    stopNames = trainStops.keySet().toArray(stopNames);
                    dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stopNames);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner1.setAdapter(dataAdapter);
                    spinner2.setAdapter(dataAdapter);
                    break;
                case R.string.request_departures:
                    JSONArray departures = jsonObj.getJSONArray("departures");
                    LinkedList<String> nextDepartures = new LinkedList<>();
                    for(int i=0; i<departures.length(); i++)
                    {
                        JSONObject departure = departures.getJSONObject(i);
                        //String[] arr = {departure.getString("scheduled_departure_utc"), String.valueOf(departure.getInt("direction_id"))};
                        nextDepartures.add(departure.getString("scheduled_departure_utc"));
                    }
                    DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    LinkedList<String> dates = new LinkedList<>();
                    for(int i=0;i<2;i++) {
                        Date endDate = iso8601.parse(nextDepartures.get(i));
                        Date startDate = new Date();
                        long different = endDate.getTime() - startDate.getTime();
                        long secondsInMilli = 1000;
                        long minutesInMilli = secondsInMilli * 60;
                        long hoursInMilli = minutesInMilli * 60;
                        long daysInMilli = hoursInMilli * 24;
                        long elapsedHours = different / hoursInMilli;
                        different = different % hoursInMilli;
                        long elapsedMinutes = different / minutesInMilli;
                        different = different % minutesInMilli;
                        String t = direction+"\n"+elapsedHours+"h "+elapsedMinutes+"m"+" | "+new SimpleDateFormat("HH:mma").format(endDate);
                        dates.add(t);
                    }
                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/dataUpdate");
                    putDataMapRequest.getDataMap().putLong("time", new Date().getTime());
                    putDataMapRequest.getDataMap().putString("train1", dates.get(0));
                    putDataMapRequest.getDataMap().putString("train2", dates.get(1));
                    PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                    putDataRequest.setUrgent();
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleClient, putDataRequest);
                    TextView temp = (TextView) findViewById(R.id.textView4);
                    temp.setText(dates.get(0)+"\n"+dates.get(1));
                    break;
                case R.string.request_direction:
                    JSONArray directions = jsonObj.getJSONArray("directions");
                    JSONObject direction = directions.getJSONObject(1);
                    direction_id = String.valueOf(direction.getInt("direction_id"));
                    break;
                default:
                    Log.i("getDataFromResponse", "request type invalid;");
            }

        }
        catch (Exception e)
        {
            Log.i("getDataFromResponse", "Error converting response to JSONObject"+e.getMessage());
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        selectedRouteId = trainRoutes.get(item);
        selectedRouteName = item;
        Toast.makeText(getApplicationContext(), "'"+item+"' selected!", Toast.LENGTH_SHORT).show();
        urlCreator = new UrlCreator();
        String[] param = {selectedRouteId};
        String[] url = {urlCreator.createUrl(R.string.request_stops, param)};
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Stops from PTV database!");
        progressDialog.show();
        progressDialog.setCancelable(false);
        new AsyncApiRequest().execute(url);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(googleClient, this);
        try {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (currentLocation != null) {
                currentLat = currentLocation.getLatitude();
                currentLng = currentLocation.getLongitude();
            }
        }catch(SecurityException e)
        {
            Log.i("onConnected", e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Wearable.DataApi.removeListener(googleClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Wearable.DataApi.removeListener(googleClient, this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.i("onDataChanged", "method called");
        for(DataEvent event: dataEventBuffer){
            Log.i("onDataChanged", "event item");
            //data item changed
            if(event.getType() == DataEvent.TYPE_CHANGED){
                Log.i("onDataChanged", "data type changed");
                DataItem item = event.getDataItem();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(item);

                //received initiation message, start the process!
                if(item.getUri().getPath().equals("/data"))
                {
                    String message = dataMapItem.getDataMap().getString("message");
                    Log.i("onDataChanged", message);
                    urlCreator = new UrlCreator();
                    String[] param = {selectedRouteId, selectedStopId1};
                    String[] url = {urlCreator.createUrl(R.string.request_departures, param)};
                    new AsyncApiRequest().execute(url);
                }
                else
                {
                    Log.i("onDataChanged", "none");
                }
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        googleClient.connect();
    }

    @Override
    public void onPause(){
        super.onPause();
        Wearable.DataApi.removeListener(googleClient, this);
        googleClient.disconnect();
    }

    class AsyncApiRequest extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls)
        {
            try
            {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    Thread.sleep(1000);
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressDialog.cancel();
            Log.i("INFO", response);
            getDataFromResponse(response);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
