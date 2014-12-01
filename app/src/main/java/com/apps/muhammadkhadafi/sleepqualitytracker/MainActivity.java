package com.apps.muhammadkhadafi.sleepqualitytracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private static final String MOVES_REDIRECT_URI = "http://test-mean-heroku.herokuapp.com/";
    private static final String MOVES_CLIENT_ID = "uxYOpga5SfmgKE9y4GN7I6ilb9Uzvvw6";
    private static final String MOVES_CLIENT_SECRET = "nqmtTvEiH3r4o1L1dr4nZLzC6yUXUrKf6ZF8f39FQX_AdxONcTf9U5GoM8tZl_O5";
    private static final String MISFIT_CLIENT_ID = "e5OZut6KodmxyrLR";
    private static final String MISFIT_CLIENT_SECRET = "LjF68gSUo6HknchD5gTO4IQXh4AaTQ8C";
    private static final String MISFIT_REDIRECT_URI = "https://glacial-ridge-1116.herokuapp.com/";
    private static final int REQUEST_AUTHORIZE = 1;

    TextView lightSensor;
    TextView soundSensor;
    Button authenticateMoves;
    TextView movesToken;
    TextView movesData;
    Button getMovesData;
    Button authenticateMisfit;
    TextView misfitToken;
    TextView misfitData;
    Button getMisfitData;

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
        movesToken = (TextView) findViewById(R.id.receivedTokenMoves);
        movesData = (TextView) findViewById(R.id.movesData);
        getMovesData = (Button) findViewById(R.id.getMovesData);
        authenticateMisfit = (Button) findViewById(R.id.authenticateMisfit);
        misfitToken = (TextView) findViewById(R.id.receivedTokenMisfit);
        misfitData = (TextView) findViewById(R.id.misfitData);
        getMisfitData = (Button) findViewById(R.id.getMisfitData);

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

        authenticateMisfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRequestAuthMisfit();
            }
        });

        getMovesData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MovesAsync().execute("data");
            }
        });

        getMisfitData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MisfitAsync().execute("data");
            }
        });

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();

        if (prefs.getString("moves_access_token", "").equals("")) {
            editor.putString("moves_access_token", "");
        }
        else {
            movesToken.setText("Token set");
            authenticateMoves.setActivated(false);
        }
        if (prefs.getString("misfit_access_token", "").equals("")) {
            editor.putString("misfit_access_token", "");
        }
        else {
            misfitToken.setText("Token set");
            authenticateMisfit.setActivated(false);
        }
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

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();

        if (prefs.getString("moves_access_token", "").equals("")) {
            editor.putString("moves_access_token", "");
        }
        else {
            movesToken.setText("Token set");
            authenticateMoves.setActivated(false);
        }
        if (prefs.getString("misfit_access_token", "").equals("")) {
            editor.putString("misfit_access_token", "");
        }
        else {
            misfitToken.setText("Token set");
            authenticateMisfit.setActivated(false);
        }
        editor.commit();

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

                        int monthCal = c.get(Calendar.MONTH) + 1;
                        int dateCal = c.get(Calendar.DATE);
                        int yearCal = c.get(Calendar.YEAR);
                        int hourCal = c.get(Calendar.HOUR_OF_DAY);
                        int minuteCal = c.get(Calendar.MINUTE);
                        int secondCal = c.get(Calendar.SECOND);

                        filename = "log" + ((monthCal < 10) ? "0" + monthCal : "" + monthCal)
                                + ((dateCal < 10) ? "0" + dateCal : "" + dateCal)
                                + yearCal + ((hourCal < 10) ? "0" + hourCal : "" + hourCal)
                                + ((minuteCal < 10) ? "0" + minuteCal : "" + minuteCal)
                                + ((secondCal < 10) ? "0" + secondCal : "" + secondCal) + ".txt";

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

                            int monthCal = c.get(Calendar.MONTH) + 1;
                            int dateCal = c.get(Calendar.DATE);
                            int yearCal = c.get(Calendar.YEAR);
                            int hourCal = c.get(Calendar.HOUR_OF_DAY);
                            int minuteCal = c.get(Calendar.MINUTE);
                            int secondCal = c.get(Calendar.SECOND);

                            String string = "log" + ((monthCal < 10) ? "0" + monthCal : "" + monthCal)
                                    + ((dateCal < 10) ? "0" + dateCal : "" + dateCal)
                                    + yearCal + ((hourCal < 10) ? "0" + hourCal : "" + hourCal)
                                    + ((minuteCal < 10) ? "0" + minuteCal : "" + minuteCal)
                                    + ((secondCal < 10) ? "0" + secondCal : "" + secondCal) + "---- Light: " +
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

        if (prefs.getString("misfit_access_token", "").equals("")) {
            editor.putString("misfit_access_token", "");

            Uri misfitUri = getIntent().getData();
            //        misfitUri.getQueryParameter("code");
            if (misfitUri != null) {
                Log.d("response", "misfit" + misfitUri);
                String misfitCode = ("" + misfitUri).substring(14);
                new MisfitAsync().execute("token", misfitCode);
            }
        }

//        else Log.d("response", "misfit nothing");
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

    private void doRequestAuthMisfit() {

        //moves://app/authorize?client_id=<your client id>&redirect_uri=<redirect_uri>&scope=<scope>

//        Uri uri = createAuthUri("moves", "app", "/authorize").build();
//        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//        try {
//            startActivityForResult(intent, REQUEST_AUTHORIZE);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(this, "Moves app not installed", Toast.LENGTH_SHORT).show();
//        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.misfitwearables" +
                ".com/auth/dialog/authorize?response_type=code&client_id=e5OZut6KodmxyrLR&redirect_uri=" +
                "https://glacial-ridge-1116.herokuapp.com/&scope=public,birthday,email"));
        startActivity(browserIntent);

    }

    private Uri.Builder createAuthUri(String scheme, String authority, String path) {
        return new Uri.Builder()
                .scheme(scheme)
                .authority(authority)
                .path(path)
                .appendQueryParameter("client_id", MOVES_CLIENT_ID)
                .appendQueryParameter("redirect_uri", MOVES_REDIRECT_URI)
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
                    MOVES_CLIENT_ID + "&client_secret=" + MOVES_CLIENT_SECRET + "&redirect_uri=" +
                    MOVES_REDIRECT_URI;
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

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
            String accessToken = prefs.getString("moves_access_token", "");

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

                SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("moves_access_token", result[1]);
                editor.commit();

                movesToken.setText("Token set");
            }
            else if (result[0] == "data") {
                movesData.setText(result[1]);
            }
        }
    }

    private class MisfitAsync extends AsyncTask<String, Void, String[]> {

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
                    movesData = getMisfitData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                returnee = new String[]{params[0], movesData};
            }

            return returnee;
        }

        private String getAccessToken(String authenticationCode) throws IOException, JSONException {

            String urlResult = "https://api.misfitwearables.com/auth/tokens/exchange?" + "" +
                    "grant_type=authorization_code&code=" + authenticationCode + "&client_id=" +
                    MISFIT_CLIENT_ID + "&client_secret=" + MISFIT_CLIENT_SECRET + "&redirect_uri=" +
                    MISFIT_REDIRECT_URI;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(urlResult);

            List nameValuePairs = new ArrayList();
            nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
            nameValuePairs.add(new BasicNameValuePair("code", authenticationCode));
            nameValuePairs.add(new BasicNameValuePair("redirect_uri", MISFIT_REDIRECT_URI));
            nameValuePairs.add(new BasicNameValuePair("client_id", MISFIT_CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("client_secret", MISFIT_CLIENT_SECRET));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity());
            Log.d("response", "tokenJSON: " + responseString);

            JSONObject jsonObject = new JSONObject(responseString);
            String accessToken = jsonObject.getString("access_token");
            Log.d("response", "token: " + accessToken);

            return accessToken;
        }

        private String getMisfitData() throws IOException, JSONException {

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
            String accessToken = prefs.getString("misfit_access_token", "");

            Date dYesterday = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
            Date dToday = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");

            String yesterdayDate = sdf.format(dYesterday);
            String todayDate = sdf.format(dToday);

            String urlResult = "https://api.misfitwearables.com/move/resource/v1/user/me/activity/" +
                    "sleeps?start_date=" + yesterdayDate + "&end_date=" + todayDate;

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urlResult);

            httpGet.addHeader("access_token", accessToken);

            Log.d("response", "url: " + urlResult);

            HttpResponse response = httpclient.execute(httpGet);
            String responseString = EntityUtils.toString(response.getEntity());
            Log.d("response", "misfitData: " + responseString);
            //JSONArray responseArray = new JSONArray(responseString);
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray jsonArray = jsonObject.getJSONArray("sleeps");

            JSONObject latestSleep = jsonArray.getJSONObject(jsonArray.length()-1);
            String startTime = latestSleep.getString("startTime");
            int sleepDuration = latestSleep.getInt("duration");

            String misfitData = "Your sleep data (" + sdf2.format(dYesterday) + "):\nSleep starts at"
                    + startTime + "\nfor the duration of " + (sleepDuration / 60) + " minutes";

            return misfitData;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if (result[0] == "token") {

                SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("misfit_access_token", result[1]);
                editor.commit();

                misfitToken.setText("Token set");
            }
            else if (result[0] == "data") {
                misfitData.setText(result[1]);
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

    private ArrayList<String[]> parseData() throws IOException {
            File sleepDir;
            sleepDir = new File(String.valueOf(getExternalFilesDir("MyFileStorage")));
            File[] sleepFiles = sleepDir.listFiles();
            for (File file : sleepFiles) {
                BufferedReader parse = new BufferedReader(new InputStreamReader(openFileInput(file.getName())));
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = parse.readLine()) != null) {
                    String[] data = new String[3];
                    line.matches("log.*----");
                    data[0] = line.substring(line.indexOf("log"), line.indexOf("----"));
                    data[1] = line.substring(line.indexOf("Light: "),line.indexOf(","));
                    data[2] = line.substring(line.indexOf("Sound: "));

                }
            }
        return
     }

}
