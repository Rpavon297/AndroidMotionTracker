package com.example.sensorcapture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import static java.lang.Thread.sleep;


public class CaptureDataActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private double[] rotation;
    private double[] acceleration;
    private Thread captureThread;
    private Thread uiThread;
    private String uri;
    private boolean connexion;
    private boolean failed;
    private final String mqttPass = "PFFVPUMW4TUF7BWO";
    private final String mqttUser = "rpavon297";
    private final String channelID = "885792";
    private final String APIKey = "H6AYWB1DOU3UKGYB";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotation = new double[]{0.0, 0.0, 0.0};
        acceleration = new double[]{0.0, 0.0, 0.0};
        uri = "tcp://mqtt.thingspeak.com:1883";
        connexion = false;
        failed = false;

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_data);

        startThread();
        //refreshUi();
    }

    private void refreshUi(){
        uiThread = new Thread(){
            @Override
            public void run(){

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        while(!isInterrupted()) {
                            try {
                                sleep(1);
                            } catch (InterruptedException e) {

                            }
                            if(!connexion && !failed){
                                TextView textView = findViewById(R.id.status);
                                textView.setText("Connecting to " + uri);
                                textView.invalidate();
                            }
                            else if(connexion){
                                TextView textView = findViewById(R.id.status);
                                textView.setText("Connected to " + uri);
                                textView.invalidate();

                                showValues();
                            }
                            else{
                                TextView textView = findViewById(R.id.status);
                                textView.setText("Failed connexion to " + uri);
                                textView.invalidate();
                            }
                        }
                    }
                });
            }
        };

        uiThread.start();
    }

    private void startThread(){
        captureThread = new Thread(){
            @Override
            public void run() {
                String clientId = MqttClient.generateClientId();
                final MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(), uri, clientId);
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setCleanSession(true);
                mqttConnectOptions.setPassword(mqttPass.toCharArray());
                mqttConnectOptions.setUserName(mqttUser);

                try {
                    client.connect(mqttConnectOptions).setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken iMqttToken) {
                            connexion = true;
                            failed = false;

                            try {
                                while (!isInterrupted()) {
                                    Thread.sleep(1000);
                                    publishValues(client);
                                }
                            } catch (Exception ignored) {
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                            connexion = false;
                            failed = true;
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }


            }
        };

        captureThread.start();
    }

    private void publishValues(MqttAndroidClient client) {
        String payload = "field1=" + acceleration[0] + "&" +
                "field2=" + acceleration[1] + "&" +
                "field3=" + acceleration[2] + "&" +
                "field4=" + rotation[0] + "&" +
                "field5=" + rotation[1] + "&" +
                "field6=" + rotation[2];
        String topic= "channels/" + channelID + "/publish/" + APIKey;

        try {
            client.publish(topic, payload.getBytes(), 0, false);
        } catch (MqttException e) {
        }
    }

    private void showValues() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView log= findViewById(R.id.status);
                log.setText("Connected to" + uri);
                log.invalidate();

                String text = "Aceleración: \nx: " + acceleration[0] + " \ny: " + acceleration[1] + " \nz: " + acceleration[2]
                        + "\n Rotación: \nx: " + rotation[0] + "\n y: " + rotation[1] + "\n z: " + rotation[2];

                TextView textView = findViewById(R.id.predictionText);
                textView.setText(text);
                textView.invalidate();
            }
        });
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
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
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

    public void stopCapturing(View view){
        Intent intent = new Intent(this, MainActivity.class);
        captureThread.interrupt();
        //uiThread.interrupt();
        startActivity(intent);
    }
}
