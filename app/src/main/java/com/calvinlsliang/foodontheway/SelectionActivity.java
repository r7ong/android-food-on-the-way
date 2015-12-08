package com.calvinlsliang.foodontheway;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.ui.PlacePicker;

public class SelectionActivity extends AppCompatActivity {

    final int PLACE_PICKER_REQUEST = 1;

    EditText etStart;
    EditText etDestination;
    EditText etFood;
    Spinner spinnerDistance;
    String radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        etStart = (EditText) findViewById(R.id.etStart);
        etDestination = (EditText) findViewById(R.id.etDestination);
        etFood = (EditText) findViewById(R.id.etFood);

        initializeSpinner();
    }

    public void initializeSpinner() {
        spinnerDistance = (Spinner) findViewById(R.id.spinnerDistance);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.distance_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDistance.setAdapter(adapter);

        spinnerDistance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection  = spinnerDistance.getItemAtPosition(position).toString();
                String distance = "500";

                switch (selection) {
                    case "Near (5 miles)":
                        distance = "8046";
                        break;
                    case "Close (10 miles)":
                        distance = "16093";
                        break;
                    case "Far (20 miles)":
                        distance = "32186";
                        break;
                    default:
                        break;

                }

                radius = distance;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void selectMap(View view) {
        String origin = etStart.getText().toString();
        String destination = etDestination.getText().toString();
        String food = etFood.getText().toString();

        Intent i = new Intent(this, MapActivity.class);
        i.putExtra("origin", origin);
        i.putExtra("destination", destination);
        i.putExtra("food", food);
        i.putExtra("radius", radius);

        startActivity(i);
    }

    public void selectLocation(View view) {
//        Toast.makeText(this, "hello", Toast.LENGTH_LONG).show();


        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Google Play Services is not available.", Toast.LENGTH_LONG).show();
        }
    }
}
