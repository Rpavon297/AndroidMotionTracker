package com.example.sensorcapture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.sensorcapture.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void captureData(View view){
        Intent intent = new Intent(this, CaptureDataActivity.class);

        String sample = "Lore ipsum";
        intent.putExtra(this.EXTRA_MESSAGE, sample);

        startActivity(intent);

    }
}
