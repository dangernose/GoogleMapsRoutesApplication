package com.example.pettrackingsystem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        TaskLoadedCallBack {

    Context context = this;

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    GoogleMap map;
    Button btnGetDirection;

    MarkerOptions placeFrom, placeTo;

    Polyline ploylineRoutes;

    EditText text_tracker_id;

    String SHARED_PREFERENCE_NAME = "TRACKER_ID";
    String TRACKER_KEY = "tracker_id";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_set_tracker_id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tracker ID");
            View view = getLayoutInflater().inflate(R.layout.dialogview, null);
            builder.setView(view);
            text_tracker_id = (EditText) view.findViewById(R.id.text_TrackerID);
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
            String trackerid = sharedPreferences.getString("tracker_id", null);
            text_tracker_id.setText(trackerid);
            builder.setCancelable(false);

            builder.setPositiveButton("Set Tracker ID", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), text_tracker_id.getText().toString(), Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit();
                    editor.putString(TRACKER_KEY, text_tracker_id.getText().toString());
                    editor.commit();
                }
            });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnGetDirection = findViewById(R.id.btnGetDirection);

        updateView("");
        btnGetDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                new FetchURL(MainActivity.this).execute(getUrl(placeTo.getPosition(), placeFrom.getPosition(), "driving"), "driving");
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
//                                Log.d("Selected Date", String.valueOf(year) + "-" + String.valueOf(monthOfYear + "-" + String.valueOf(dayOfMonth)));

                                map.clear();
                                DatabaseAdapter adapter = new DatabaseAdapter(context);
                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, year);
                                cal.set(Calendar.MONTH, monthOfYear);
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                String date = (new SimpleDateFormat("yyyy-MM-dd")).format(cal.getTime());
                                Log.d("Date Selected", date);
                                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE);
                                String trakcerid = sharedPreferences.getString(TRACKER_KEY,"NULL");
                                List<PetLocation> petLocationList = adapter.getAllPetLocations(date,trakcerid);
                                Log.d("Number of Location", String.valueOf(petLocationList.size()));

                                for (PetLocation petlocation:petLocationList) {
                                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(petlocation.getLat(), petlocation.getLng())).title(petlocation.getTime());
                                    Log.d("Data : ",petlocation.getDate() + " : " +  String.valueOf(petlocation.getLat()) + "," + String .valueOf(petlocation.getLng()));
                                    map.addMarker(markerOptions);
                                }

                                for(int i = 1;i<petLocationList.size();i++){
                                    LatLng toLatLng = new LatLng(petLocationList.get(i-1).getLat(),petLocationList.get(i-1).getLng());
                                    LatLng fromLatLng = new LatLng(petLocationList.get(1).getLat(),petLocationList.get(i).getLng());
                                    MarkerOptions to = new MarkerOptions().position(toLatLng).title(petLocationList.get(i-1).getTime());
                                    MarkerOptions from = new MarkerOptions().position(fromLatLng).title(petLocationList.get(i).getTime());

                                    new FetchURL(MainActivity.this).execute(getUrl(to.getPosition(), from.getPosition(), "driving"), "driving");
                                }
                                if(petLocationList.size()!=0){
                                    map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(petLocationList.get(0).getLat(),petLocationList.get(0).getLng())));
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(petLocationList.get(0).getLat(),petLocationList.get(0).getLng()), 16));
                                }

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

            }
        });

        try{
            MQTTClient client = new MQTTClient(this);
            client.connectToMQTT();
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }

        placeFrom = new MarkerOptions().position(new LatLng(27.658143, 85.3199503)).title("Location From");
//        DatabaseAdapter adapter = new DatabaseAdapter(context);
//        Long a = adapter.InserPetLocation(27.658143,85.3199503,"2019-12-19","7:55","pet01");
//        Log.d("PetLocation Inserted", String.valueOf(a));
//        Long b = adapter.InserPetLocation(27.667491,85.3208583,"2019-12-19","8:55","pet01");
//        Log.d("PetLocation Inserted", String.valueOf(b));

        placeTo = new MarkerOptions().position(new LatLng(27.667491, 85.3208583)).title("Location To");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);
        mapFragment.getMapAsync(this);

        String url = getUrl(placeTo.getPosition(), placeFrom.getPosition(), "driving");



//        Code to get your current Location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG,"isServicesOK : Checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(available == ConnectionResult.SUCCESS){
            Log.d(TAG,"isServiceOk : Google Play Services is working");
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG,"isServiceOk : an error");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this,"Can't Make Maps Requests", Toast.LENGTH_LONG).show();
        }
        return  false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setMyLocationEnabled(true);

        map.getMyLocation();
        DatabaseAdapter adapter = new DatabaseAdapter(context);
        String date = (new SimpleDateFormat("yyyy/MM/dd")).format(new Date());
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        String trakcerid = sharedPreferences.getString(TRACKER_KEY,"NULL");
        List<PetLocation> petLocationList = adapter.getAllPetLocations(date,trakcerid);
        Log.d("Number of Location", String.valueOf(petLocationList.size()));

        for (PetLocation petlocation:petLocationList) {
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(petlocation.getLat(), petlocation.getLng())).title(petlocation.getTime());
            map.addMarker(markerOptions);
        }

        for(int i = 1;i<petLocationList.size();i++){
            LatLng toLatLng = new LatLng(petLocationList.get(i-1).getLat(),petLocationList.get(i-1).getLng());
            LatLng fromLatLng = new LatLng(petLocationList.get(1).getLat(),petLocationList.get(i).getLng());
            MarkerOptions to = new MarkerOptions().position(toLatLng);
            MarkerOptions from = new MarkerOptions().position(fromLatLng);

            new FetchURL(MainActivity.this).execute(getUrl(to.getPosition(), from.getPosition(), "driving"), "driving");
        }
        if(petLocationList.size()!=0){
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(petLocationList.get(0).getLat(),petLocationList.get(0).getLng())));
        }

        if(petLocationList.size()!=0){
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(petLocationList.get(0).getLat(),petLocationList.get(0).getLng())));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(petLocationList.get(0).getLat(),petLocationList.get(0).getLng()), 16));
        }

    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (ploylineRoutes != null)
            ploylineRoutes.remove();
        ploylineRoutes = map.addPolyline((PolylineOptions) values[0]);
    }

    public void updateView(String sensorMessage){
        Log.d("Sensor Message in View", sensorMessage);
        try{
            if(sensorMessage == null || sensorMessage == "") {

            }else{
                final String tempSensorMessage = sensorMessage;


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        TextView updateField = (TextView) findViewById(R.id.txtData);
                        updateField.setText(tempSensorMessage + " " + df.format(new Date()));
                    }
                });

                String latlng[] = sensorMessage.split(",");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                DatabaseAdapter adapter = new DatabaseAdapter(this.context);
                String MaxTime = adapter.getMaxTime(dateFormat.format(new Date()));
                if(MaxTime != null) {
                    Log.d("Max Time",MaxTime);
                }

                String petid = getPetID();

                Log.d("Datas :", Double.parseDouble(latlng[0]) + "," +
                        Double.parseDouble(latlng[1]) + "," +
                        dateFormat.format(new Date()) + "," +
                        timeFormat.format(new Date()) + "," +
                        petid);

                long id = adapter.InserPetLocation(
                        Double.parseDouble(latlng[0]),
                        Double.parseDouble(latlng[1]),
                        dateFormat.format(new Date()),
                        timeFormat.format(new Date()),
                        petid
                );

                Log.d("Inserted ID", String.valueOf(id));
            }
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }

    }

    public void createNotification(String notificationTitle, String notificationMessage) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.notifiy_icon_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage);

        Intent resultIntent = new Intent(getApplicationContext(),MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100,mBuilder.build());
    }

    public String getPetID(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        return(sharedPreferences.getString(TRACKER_KEY,""));
    }
}
