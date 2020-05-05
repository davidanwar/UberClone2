package com.example.uberclone2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView listView;
    private ArrayList<String> nearByDriveRequest;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengerLatitude, passengerLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnGetRequests = findViewById(R.id.btnGetRequest);
        btnGetRequests.setOnClickListener(this);

        listView = findViewById(R.id.listRequest);
        nearByDriveRequest = new ArrayList<>();
        passengerLatitude = new ArrayList<>();
        passengerLongitude = new ArrayList<>();
        adapter = new ArrayAdapter(DriverRequestListActivity.this, android.R.layout.simple_list_item_1, nearByDriveRequest);
        listView.setAdapter(adapter);
        // agar tidak mengulang menampilkan data di list view
        nearByDriveRequest.clear();

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

        }



        listView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.itemDriverLogout){
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        

        if (Build.VERSION.SDK_INT < 23){
            Location currentPessengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentPessengerLocation);
        } else if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String [] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            } else {
                // kita tidak butuh lagi request update location karena sudah dibuat di onCreat Methode DriverActivity
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPessengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentPessengerLocation);
            }
        }
    }

    private void updateRequestListView(Location driverLocation) {
        if (driverLocation != null) {
            // agar tidak mengulang menampilkan data di list view

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());
            ParseQuery<ParseObject> parseCarQuery = ParseQuery.getQuery("RequestCar");
            parseCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            parseCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {

                        if (objects.size() > 0 && e == null) {
                            if (nearByDriveRequest.size() > 0 ){
                                nearByDriveRequest.clear();
                            }
                            if (passengerLongitude.size() > 0){
                                passengerLongitude.clear();
                            }
                            if (passengerLatitude.size() > 0){
                                passengerLatitude.clear();
                            }
                            for (ParseObject nearRequest : objects) {
                                // di parse server "PassengerLocation" adalah tipe GeoPoint maka supaya compatible diberi typecasting (ParseGeoPoint)
                                // jika di parse server buka bertipe GeoPoint kemudian di typecasting dengan GeoPoint maka aplikasi akan crash
                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRequest.get("passengerLocation");
                                Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo(pLocation);
                                // misal nilai milesDistanceToPassenger = 5.82463534436743 kemudian kita kali dengan 10
                                // hasilnya 58.2463534436743
                                // dibulatkan dengan fungsi Match.round jadi 57
                                // kita bagi 10 sehingga nilai yang tampil ke user adalah 5.7
                                float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10);
                                nearByDriveRequest.add("There are " + roundedDistanceValue + " miles to " + nearRequest.get("username"));
                                passengerLatitude.add(pLocation.getLatitude());
                                passengerLongitude.add(pLocation.getLongitude());
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(DriverRequestListActivity.this, "Sorry There no Request", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });



        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                // kode di bawah tidak butuh lagi karena sudah dideklarasikan di clickMethode.
                // jika kode ini ditulis maka akan terjadi penguilangan dua kali pada listView
                //Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //updateRequestListView(currentDriverLocation);
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
