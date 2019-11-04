package com.example.sensorcapture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import static java.lang.Thread.sleep;


public class CaptureDataActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private double[] rotation;
    private double[] acceleration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotation = new double[]{0.0, 0.0, 0.0};
        acceleration = new double[]{0.0, 0.0, 0.0};

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_data);

        capture();
    }

    private void capture() {
       // while(true){
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String text = "Aceleración: x: " + acceleration[0] + " y: " + acceleration[1] + " z: " + acceleration[2]
                    + "\n Rotación: x: " + rotation[0] + " y: " + rotation[1] + " z: " + rotation[2];

            TextView textView = findViewById(R.id.predictionText);
            textView.setText(text);
        //}
    }

    private void getAccelerometer(SensorEvent event) {

        double alpha = 0.8;
        double[] gravity = new double[3];

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        acceleration[0] = event.values[0] - gravity[0];
        acceleration[1] = event.values[1] - gravity[1];
        acceleration[2] = event.values[2] - gravity[2];
    }

    private void getGyroscope(SensorEvent event) {

        rotation[0] = event.values[0];
        rotation[1] = event.values[1];
        rotation[2] = event.values[2];
    }

    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            getAccelerometer(event);
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            getGyroscope(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
