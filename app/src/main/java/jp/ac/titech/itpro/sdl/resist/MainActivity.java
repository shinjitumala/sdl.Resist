package jp.ac.titech.itpro.sdl.resist;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.sql.Time;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private RotationView rotationView;
    private SensorManager manager;
    private Sensor gyroscope;

    // storage for the last time that there was an input from the gyroscope
    private long last_time;

    // storage for getting the average of N data
    private final int N = 2;
    private final float alpha = (float) (N - 1) / N;
    private float last_omega;
    private float last_rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        rotationView = findViewById(R.id.rotation_view);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (manager == null) {
            Toast.makeText(this, R.string.toast_no_sensor_manager, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        gyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope == null) {
            Toast.makeText(this, R.string.toast_no_gyroscope, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        manager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float omegaZ = event.values[2];  // z-axis angular velocity (rad/sec)
        // TODO: calculate right direction that cancels the rotation
        // use the average of the last N data as omegaZ
        omegaZ = alpha * last_omega + (1 - alpha) * omegaZ;


        long current_time = event.timestamp;
        float current_rotation;

        current_rotation = last_rotation + omegaZ * (float) (current_time - last_time) / 1000000000;

        rotationView.setDirection(current_rotation);
//        Log.d(TAG, "onSensorChanged{ omega: " + omegaZ + ", dt: " + ((float) (current_time - last_time) / 1000000) + ", dr: " + (new_rotation - current_rotation) + "}");

        last_time = current_time;
        last_omega = omegaZ;
        last_rotation = current_rotation;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged: accuracy=" + accuracy);
    }
}
