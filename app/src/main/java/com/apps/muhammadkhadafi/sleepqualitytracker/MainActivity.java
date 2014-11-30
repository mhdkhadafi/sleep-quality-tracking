package com.apps.muhammadkhadafi.sleepqualitytracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;


public class MainActivity extends Activity {

    private static final String REDIRECT_URI = "http://test-mean-heroku.herokuapp.com/";
    private static final String CLIENT_ID = "uxYOpga5SfmgKE9y4GN7I6ilb9Uzvvw6";
    private static final String CLIENT_SECRET = "nqmtTvEiH3r4o1L1dr4nZLzC6yUXUrKf6ZF8f39FQX_AdxONcTf9U5GoM8tZl_O5";
    private static final int REQUEST_AUTHORIZE = 1;

    TextView lightSensor;
    TextView soundSensor;
    Button authenticateMoves;
    TextView movesToken;
    TextView movesData;
    Button getMovesData;
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
    String movesAuthCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lightSensor = (TextView) findViewById(R.id.light);
        soundSensor = (TextView) findViewById(R.id.sound);
        authenticateMoves = (Button) findViewById(R.id.authenticateMoves);
        movesToken = (TextView) findViewById(R.id.receivedToken);
        movesData = (TextView) findViewById(R.id.movesData);
        getMovesData = (Button) findViewById(R.id.getMovesData);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        sensorManager.registerListener(new LightListener(), lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        writeActive = false;
        isWriting = false;
        repeatedHandler = 0;

        authenticateMoves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRequestAuthInApp();
            }
        });

        getMovesData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MovesAsync().execute("data");
            }
        });

        SharedPreferences movesPrefs = getApplicationContext().getSharedPreferences("MovesPrefs", 0);
        SharedPreferences.Editor editor = movesPrefs.edit();
        editor.putString("access_token", "");
        editor.commit();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_AUTHORIZE:
                Uri resultUri = data.getData();
                Log.d("result", resultUri + "");
                movesAuthCode = resultUri.getQueryParameter("code");

                new MovesAsync().execute("token", movesAuthCode);
        }

    }

    private void doRequestAuthInApp() {

        //moves://app/authorize?client_id=<your client id>&redirect_uri=<redirect_uri>&scope=<scope>

        Uri uri = createAuthUri("moves", "app", "/authorize").build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivityForResult(intent, REQUEST_AUTHORIZE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Moves app not installed", Toast.LENGTH_SHORT).show();
        }

    }

    private Uri.Builder createAuthUri(String scheme, String authority, String path) {
        return new Uri.Builder()
                .scheme(scheme)
                .authority(authority)
                .path(path)
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("scope", "location activity");
    }

    protected static void startTimer() {
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 100);
    }

    private class MovesAsync extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            String[] returnee = new String[1];

            if (params[0] == "token") {
                String accessToken = "";
                try {
                    accessToken = getAccessToken(params[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                returnee = new String[]{params[0], accessToken};
//                return returnee;
            }
            else if (params[0] == "data") {
                String movesData = "";
                try {
                    movesData = getMovesData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                returnee = new String[]{params[0], movesData};
            }

            return returnee;
        }

        private String getAccessToken(String authenticationCode) throws IOException, JSONException {

            String urlResult = "https://api.moves-app.com/oauth/v1/access_token?" + "" +
                    "grant_type=authorization_code&code=" + authenticationCode + "&client_id=" +
                    CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&redirect_uri=" + REDIRECT_URI;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(urlResult);

            HttpResponse response = httpclient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity());
            Log.d("response", "tokenJSON: " + responseString);

            JSONObject jsonObject = new JSONObject(responseString);
            String accessToken = jsonObject.getString("access_token");
            Log.d("response", "token: " + accessToken);

            return accessToken;
        }

        private String getMovesData() throws IOException, JSONException {

            SharedPreferences movesPrefs = getApplicationContext().getSharedPreferences("MovesPrefs", 0);
            String accessToken = movesPrefs.getString("access_token", "");

            Date d = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");

            String yesterdayDate = sdf.format(d);

            String urlResult = "https://api.moves-app.com/api/1.1/user/summary/daily/" +
                    yesterdayDate + "?access_token=" + accessToken;
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urlResult);

            Log.d("response", "url: " + urlResult);

            HttpResponse response = httpclient.execute(httpGet);
            String responseString = EntityUtils.toString(response.getEntity());
            Log.d("response", "movesData: " + responseString);
            Log.d("response", "movesData: " + responseString.substring(1, responseString.length() - 1));
            //JSONArray responseArray = new JSONArray(responseString);
            JSONObject jsonObject = new JSONObject(responseString.substring(1, responseString.length() - 1));
//            JSONArray jsonArray = jsonObject.names();
//
//            Log.d("response", jsonArray.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("summary");

            Log.d("response", "summary: " + jsonArray);

            int totalSteps = 0;
            int walkingDuration = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d("response", "activity: " + jsonArray.getJSONObject(i).getString("activity"));
                if (jsonArray.getJSONObject(i).getString("activity").equals("walking")) {
                    totalSteps = jsonArray.getJSONObject(i).getInt("steps");
                    walkingDuration = jsonArray.getJSONObject(i).getInt("duration");
                    Log.d("response", "result: " + totalSteps + "-" + walkingDuration);
                }
                else continue;
            }

            String movesData = "Your activity data (" + sdf2.format(d) + "):\n" + totalSteps +
                    " walking steps\n" + walkingDuration + " walking duration";

            return movesData;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if (result[0] == "token") {

                SharedPreferences movesPrefs = getApplicationContext().getSharedPreferences("MovesPrefs", 0);
                SharedPreferences.Editor editor = movesPrefs.edit();
                editor.putString("access_token", result[1]);
                editor.commit();

                movesToken.setText("Token set");
            }
            else if (result[0] == "data") {
                movesData.setText(result[1]);
            }
        }
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
