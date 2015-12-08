package com.calvinlsliang.foodontheway;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.ui.PlacePicker;

public class SelectionActivity extends AppCompatActivity {

    final int PLACE_PICKER_REQUEST = 1;

    EditText etStart;
    EditText etDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        etStart = (EditText) findViewById(R.id.etStart);
        etDestination = (EditText) findViewById(R.id.etDestination);
    }

    public void selectMap(View view) {
        String origin = etStart.getText().toString();
        String destination = etDestination.getText().toString();

        Intent i = new Intent(this, MapActivity.class);
        i.putExtra("origin", origin);
        i.putExtra("destination", destination);
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
