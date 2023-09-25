package com.example.test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import kotlinx.coroutines.GlobalScope;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_SET_DESTINATION = 1;
    private Button setDestinationButton;
    private Button showDetailsButton;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationManager locationManager;
    private LocationListener locationListener;
    public LatLng destinationLatLng;
    public LatLng currentLatLng;
    private String destinationName;
    public String point;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        showDetailsButton = findViewById(R.id.showDetails);
        setDestinationButton = findViewById(R.id.setDestinationButton);
        showDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RouteDetails.class);
                startActivityForResult(intent, REQUEST_SET_DESTINATION);

            }
        });
        setDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open a screen for users to set the destination (e.g., another activity).
                // You need to implement this screen to set the destination coordinates.
                // For simplicity, you can use a dummy destination.
//                destinationLatLng = new LatLng(37.7749, -122.4194); // San Francisco, CA
//                drawDestinationMarker(destinationLatLng);
                Intent intent = new Intent(MainActivity.this, DestinationSettingActivity.class);
                startActivityForResult(intent, REQUEST_SET_DESTINATION);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        createLocationRequest();
        createLocationCallback();

    }
// Make an HTTP request to the Directions API using Retrofit or OkHttp.
// Parse the JSON response to extract route information.
// Display the route on the map using PolylineOptions.

// Example request:
// https://maps.googleapis.com/maps/api/directions/json?origin=originLatLng&destination=destinationLatLng&key=YOUR_API_KEY

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Request location updates when the map is ready.
        requestLocationUpdates();

        // Zoom to a default location (e.g., user's current location).

        // For now, let's assume the user is in San Francisco.
        getLocation();
        mMap.setOnMyLocationChangeListener(e -> {getLocation();});



    }

    public void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // This method is called when the location changes
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();

                // Now you have the user's current latitude and longitude
                // You can use these values as needed in your application
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                drawUserLocationMarker(currentLatLng);
                //Log.d("lokacja", String.valueOf(currentLatLng));
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                //locationManager.removeUpdates(locationListener);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Handle location provider status changes if needed
            }

            @Override
            public void onProviderEnabled(String provider) {
                // Handle provider enabled if needed
            }

            @Override
            public void onProviderDisabled(String provider) {
                // Handle provider disabled if needed
            }
//            LatLng destinationLatLng = data.getParcelableExtra("destination");
            //requestRoad(currentLatLng);
        };

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Update location every 5 seconds
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update the map with the user's current location.
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    drawUserLocationMarker(currentLatLng);
                }
            }
        };
    }
//    public List<LatLng> decodePolyline(String encodedPolyline) {
//        // Decode the encoded polyline using the "google-maps-utils" library
//        EncodedPolylineDecoder decoder = new EncodedPolylineDecoder();
//        List<LatLng> decodedPath = decoder.decode(encodedPolyline);
//
//        return decodedPath;
//    }
    public void requestRoute() {
        // Use a routing API (e.g., Google Maps Directions API) to request a route.
        // Handle API request, response parsing, and error handling here.

        // For simplicity, we'll just draw a straight line as a sample route.
        double currLat = currentLatLng.latitude;
        double currLng = currentLatLng.longitude;

        double destLat = destinationLatLng.latitude;
        double destLng = destinationLatLng.longitude;
        OkHttpClient client = new OkHttpClient();
        Log.d("destLat", String.valueOf(destLat));
        Log.d("destLng", String.valueOf(destLng));
//        Log.d("currentLatLng", Double.valueOf(currentLatLng));
        Request request = new Request.Builder()
                .url("https://maps.googleapis.com/maps/api/directions/json?destination="+destLat+","+destLng+"&origin="+currLat+","+currLng+"&key=AIzaSyCp9sYZLL-H471rZqbcSd5Me-F530vJKZY")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("test", "dziala");
                if(response.isSuccessful()){
                    String responseBody= response.body().string();

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        Log.d("jsonResponse", String.valueOf(jsonResponse));
                        JSONArray routes = jsonResponse.getJSONArray("routes");
                        JSONObject polyline = routes.getJSONObject(0);
                        JSONArray legs = polyline.getJSONArray("legs");
                        JSONObject distance = legs.getJSONObject(0);
                        JSONObject duration = distance.getJSONObject("distance");
                        String text = duration.getString("text");
                        JSONObject line = polyline.getJSONObject("overview_polyline");
                        String point = line.getString("points");
                        //JSONObject polyline = routes.getJSONObject("overview_polyline");
                        //String encodedPolyline = point.routes[0].overviewPolyline.getEncodedPath();
                        Log.d("polyline", String.valueOf(polyline));
                        Log.d("legs", String.valueOf(legs));
                        Log.d("distance", String.valueOf(text));
                        //Log.d("duration", String.valueOf(duration));
                       // List<LatLng> decodedPath = decodePolyline(point);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        PolylineOptions polylineOptions = new PolylineOptions()
                .add(currentLatLng, destinationLatLng)
                //.addAll(point)
                .width(5)
                .color(Color.BLUE);

        mMap.addPolyline(polylineOptions);
    }
    private void requestLocationUpdates() {
        // Request location updates.
    }

    private void drawUserLocationMarker(LatLng latLng) {


        //mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
//                latLng,
//                15,
//                1,
//                1
//        )));
    }


    private void drawDestinationMarker(LatLng latLng, String destinationName) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(""+destinationName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        RelativeLayout myLayout = findViewById(R.id.main);
        Button myButton = new Button(this);
        myButton.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.ALIGN_PARENT_BOTTOM));

        myLayout.addView(myButton);


        // Calculate and display the route from the current location to the destination.
        // You'll need to implement this using a routing API.
        // Also, handle the route details and turn-by-turn directions.
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SET_DESTINATION && resultCode == RESULT_OK) {
            // Get the destination coordinates from the result intent.
            destinationLatLng = data.getParcelableExtra("destination");
           String destinationName = data.getParcelableExtra("City");

            // Call the method to draw the destination marker.
            Log.d("po inetnecie", String.valueOf(destinationLatLng));
            drawDestinationMarker(destinationLatLng, destinationName);
            requestRoute();
            showDetailsButton.setVisibility(View.VISIBLE);
        }
    }
}