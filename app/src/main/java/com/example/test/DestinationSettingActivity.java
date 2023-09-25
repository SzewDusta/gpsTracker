package com.example.test;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DestinationSettingActivity extends AppCompatActivity {

    private EditText destinationEditText;
    private Button setDestinationButton;
    PlacesClient placesClient;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_setting);

        destinationEditText = findViewById(R.id.destinationEditText);
        setDestinationButton = findViewById(R.id.setDestinationButton);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        setDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String destinationAddress = destinationEditText.getText().toString();

                // Here, you can use a geocoding API (like Google Places API)
                // to convert the destination address into LatLng coordinates.
                // For simplicity, we'll use a predefined destination.
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://maps.googleapis.com/maps/api/geocode/json?address="+destinationAddress+"&key=AIzaSyCp9sYZLL-H471rZqbcSd5Me-F530vJKZY")
                        .get()
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Handle network errors here
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            //Log.d("chuj", responseBody);
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                JSONArray resultsArray = jsonResponse.getJSONArray("results");
                                if (resultsArray.length() > 0) {

                                    JSONObject firstResult = resultsArray.getJSONObject(0);

                                    // Get the "geometry" object
                                    JSONObject geometryObject = firstResult.getJSONObject("geometry");

                                    // Get the "location" object
                                    JSONObject locationObject = geometryObject.getJSONObject("location");

                                    // Get the "lat" and "lng" values from the "location" object
                                    Double lat = locationObject.getDouble("lat");
                                    Double lng = locationObject.getDouble("lng");
                                    LatLng destinationLatLng = new LatLng(lat, lng);

                                    JSONArray addressObject = firstResult.getJSONArray("address_components");
                                    JSONObject dupa = addressObject.getJSONObject(0);

                                    String destinationName = dupa.getString("long_name");
                                   // JSONArray addressArray = addressObject.getJSONArray("address_components");
                                    //String destinationName = addressObject.getString("long_name");
                                    Log.d("address", String.valueOf(dupa));
                                    Log.d("latlng", String.valueOf(destinationLatLng));

                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("destination", destinationLatLng);
                                   // resultIntent.putExtra("RouteDetails", )
                                    resultIntent.putExtra("City", destinationName);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();

                                }
                            }catch (JSONException e) {
                                throw new RuntimeException(e);
                            }


                            // Process the response data here
                            // Note: You should perform UI updates on the UI thread if needed.
                        } else {
                            // Handle non-successful responses here (e.g., 404, 500, etc.)
                        }
                    }
                });



                // Pass the destination coordinates back to the main activity.


            }
        });
    }
}