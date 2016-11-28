package com.example.zach.spincycle;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.example.zach.spincycle.MainActivity.SPIN_COUNT;

public class Spin_Display extends AppCompatActivity  implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mOrient;
    private boolean firstRun = true;
    private int lastAngle = 0;
    private int totalRotation = 0;
    private int numSpins = 0;
    private int count;
    boolean soundPlayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        count = intent.getIntExtra(SPIN_COUNT,15);
        setContentView(R.layout.activity_spin__display);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrient = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        int yaw = (int) event.values[0];
        TextView spin_count = (TextView) findViewById(R.id.spin_count);
        checkSpins(yaw);
        int final_count = count - numSpins;
        spin_count.setText(String.valueOf(final_count));
        synchronized (this) {
            if (final_count == 0 && !soundPlayed) {
                //Intent intent = new Intent(Spin_Display.this, MainActivity.class);
                Intent intent = new Intent(Spin_Display.this, LineActivity.class);
                startActivity(intent);
                playSound();
                soundPlayed = true;
            }
        }
    }

    public synchronized void playSound(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public final void checkSpins(int yaw) {
        if(firstRun) {
            lastAngle = yaw;
            firstRun = false;
        } else {
            int change = (yaw - lastAngle);
            if(Math.abs(change) > 180 ) {
                int new1 = 0;
                int new2 = 0;
                if(yaw > 270 && lastAngle < 90) {
                    new1 = -(360 - yaw);
                    new2 = -lastAngle;
                }
                if(yaw < 90 && lastAngle > 270) {
                    new1 = yaw;
                    new2 = 360 - lastAngle;

                }
                change = new1 + new2;
            }
            totalRotation += change;
            lastAngle = yaw;
            int div = totalRotation / 360;
            if(Math.abs(div) == 1) {
                numSpins++;
                //so we don't discard a few degrees each spin
                totalRotation = (Math.abs(totalRotation)-360)*div;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mOrient, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

}
