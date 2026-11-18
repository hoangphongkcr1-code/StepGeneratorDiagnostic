
package com.example.stepgenerator;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounter, stepDetector;
    private TextView status, hardwareSteps, demoSteps;
    private int demoCount = 0;
    private float firstValue = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 40, 32, 32);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("STEP GENERATOR");
        title.setTextSize(25);
        layout.addView(title);

        status = addText(layout, "");
        hardwareSteps = addText(layout, "Android sensor steps: 0");
        demoSteps = addText(layout, "Demo steps: 0");

        Button add = new Button(this);
        add.setText("DEMO +1 STEP");
        add.setOnClickListener(v -> {
            demoCount++;
            demoSteps.setText("Demo steps: " + demoCount);
        });
        layout.addView(add);

        Button reset = new Button(this);
        reset.setText("RESET DEMO");
        reset.setOnClickListener(v -> {
            demoCount = 0;
            demoSteps.setText("Demo steps: 0");
        });
        layout.addView(reset);

        TextView note = new TextView(this);
        note.setText("\\nDemo counter is local only. It does not create Android sensor events or send steps to Strava.");
        layout.addView(note);

        setContentView(layout);

        status.setText(
            "STEP_COUNTER: " + (stepCounter == null ? "NOT AVAILABLE" : "AVAILABLE") +
            "\\nSTEP_DETECTOR: " + (stepDetector == null ? "NOT AVAILABLE" : "AVAILABLE")
        );

        if (Build.VERSION.SDK_INT >= 29 &&
            checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100
            );
        }
    }

    private TextView addText(LinearLayout layout, String value) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(18);
        t.setPadding(0, 20, 0, 20);
        layout.addView(t);
        return t;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounter != null)
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        if (stepDetector != null)
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (firstValue < 0) firstValue = event.values[0];
            long steps = Math.max(0, (long)(event.values[0] - firstValue));
            hardwareSteps.setText("Android sensor steps: " + steps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
