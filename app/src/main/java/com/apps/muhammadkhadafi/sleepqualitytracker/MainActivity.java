package com.apps.muhammadkhadafi.sleepqualitytracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;


public class MainActivity extends Activity {

    TextView lightSensor;
    TextView soundSensor;
    SoundMeter soundMeter;
    public static Handler mHandler;
    public static Timer mTimer = new Timer();

    public static PendingIntent pi;
    public static AlarmManager am;

    String filename;
    FileOutputStream outputStream;
    Boolean isWriting;
    Boolean writeActive;
    int repeatedHandler;
    File myExternalFile;
    FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lightSensor = (TextView) findViewById(R.id.light);
        soundSensor = (TextView) findViewById(R.id.sound);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        sensorManager.registerListener(new LightListener(), lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        writeActive = false;
        isWriting = false;
        repeatedHandler = 0;
    }

    @Override
    protected void onPause(){
        super.onPause();

        soundMeter.stop();

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeActive = false;
        isWriting = false;
    }


    @Override
    protected void onResume(){
        super.onResume();

        isWriting = true;

        soundMeter = new SoundMeter();
        try {
            soundMeter.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                soundSensor.setText(soundMeter.getAmplitude() + "");

                if (isWriting) {
                    if (!writeActive) {
                        Calendar c = Calendar.getInstance();

                        filename = "log" + c.get(Calendar.MONTH) + c.get(Calendar.DATE) +
                                c.get(Calendar.YEAR) + c.get(Calendar.HOUR) +
                                c.get(Calendar.MINUTE) + c.get(Calendar.SECOND) + ".txt";

                        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                        }
                        else {
                            myExternalFile = new File(getExternalFilesDir("MyFileStorage"), filename);
                        }

                        try {
                            fos = new FileOutputStream(myExternalFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        writeActive = true;

                    } else {
                        if (repeatedHandler == 0) {
                            Calendar c = Calendar.getInstance();

                            String string = "log" + c.get(Calendar.MONTH) + c.get(Calendar.DATE) +
                                    c.get(Calendar.YEAR) + c.get(Calendar.HOUR) +
                                    c.get(Calendar.MINUTE) + c.get(Calendar.SECOND) + "---- Light: " +
                                    lightSensor.getText() + ", Sound: " + soundSensor.getText() + "\n";

                            try {
                                fos.write((string + "\n").getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (repeatedHandler < 300) repeatedHandler++;
                    else repeatedHandler = 0;
                }
            }
        };

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 0F;
        getWindow().setAttributes(layout);
        startTimer();
    }


    public void writeLog() {
        String filename = "myfile";
        String string = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void startTimer() {
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class LightListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            long timestamp = event.timestamp;
            float value = event.values[0];

            lightSensor.setText("" + value);
//            if (soundMeter != null) soundSensor.setText(soundMeter.getAmplitude() + "");
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
