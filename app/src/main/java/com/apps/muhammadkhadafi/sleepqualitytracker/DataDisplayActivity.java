package com.apps.muhammadkhadafi.sleepqualitytracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mhd.khadafi on 12/3/2014.
 */
public class DataDisplayActivity extends Activity{

    Button showMoves;
    Button showMisfit;
    Button showEnv;
    Button buttonBack;
    TextView dataView;

    String stringToGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        showMoves = (Button) findViewById(R.id.show_moves);
        showMisfit = (Button) findViewById(R.id.show_misfit);
        showEnv = (Button) findViewById(R.id.show_env);
        dataView = (TextView) findViewById(R.id.data_text);
        buttonBack = (Button) findViewById(R.id.buttonBack);

        showMoves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTextFile("moves");
            }
        });

        showMisfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("response", "clicked misfit");
                readTextFile("misfit");

//                makeGraph(stringToGraph);
            }
        });

        showEnv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTextFile("env");
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(i);
//                makeSleepGraph(stringToGraph);
                makeSoundGraph(stringToGraph);
            }
        });
    }

    private void readTextFile(String testType) {
        try {
            File fileToOpen = new File("file not found");
            File[] files = getExternalFilesDir("MyFileStorage").listFiles();
            if (testType.equals("env")) {
                for (int i = 0; i < files.length; i++) {
                    // Get the latest environment file
//                    if (files[i].getName().substring(0, 3).equals("env")) {
//                        if (files[i].length() > 0) {
//                            if (fileToOpen.getName().equals("")) fileToOpen = files[i];
//                            if (fileToOpen.lastModified() < files[i].lastModified()) {
//                                fileToOpen = files[i];
//                            }
//                        }
//                    }
                    if (files[i].getName().equals("env12082014172025.txt")) {
                        fileToOpen = files[i];
                        break;
                    }
                }
            }
            else if (testType.equals("moves")) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
                Date d = new Date(System.currentTimeMillis());
//                fileToOpen = new File("no new move file, get first");

                for (int i = 0; i < files.length; i++) {
//                    Log.d("response", files[i].getName());
                    if (files[i].getName().substring(0, 5).equals("moves")) {
                        if (sdf.format(d).equals(files[i].getName().substring(5, 13))) {
                            fileToOpen = files[i];
                        }
                    }
                }
            }
            else if (testType.equals("misfit")) {
                Log.d("response", "in misfit" + files.length);
                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
                Date d = new Date(System.currentTimeMillis());
//                fileToOpen = new File("no new move file, get first");

                for (int i = 0; i < files.length; i++) {

                    if (files[i].getName().substring(0, 6).equals("misfit")) {
                        Log.d("response", sdf.format(d));
                        Log.d("response", files[i].getName());
                        if (sdf.format(d).equals(files[i].getName().substring(6, 14))) {
                            fileToOpen = files[i];
                        }
                    }
                }
            }


            if (fileToOpen.exists()) {
                FileInputStream fIn = new FileInputStream(fileToOpen);
                BufferedReader myReader = new BufferedReader(
                        new InputStreamReader(fIn));
                String aDataRow = "";
                String aBuffer = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    if (testType.equals("misfit")) {
                        aBuffer += parseMisfit(aDataRow) + "\n";
                    }
                    else if (testType.equals("env")) {
                        aBuffer += parseEnv(aDataRow) + "\n";
                    }
                    else {
                        aBuffer += aDataRow + "\n";
                    }

                }
                myReader.close();

                dataView.setText(aBuffer);
                stringToGraph = aBuffer;
//                if (testType.equals("misfit")) {
//                    makeGraph(aBuffer);
//                }
            }
            else {
                dataView.setText("no files yet, get first?");
            }
        }
        catch (Exception e) {
//            Toast.makeText(getBaseContext(), e.getMessage(),
//                    Toast.LENGTH_SHORT).show();
            dataView.setText("no files yet, get first?");
        }

    }

    private float[] normalize(float[] toNormalize) {
        float min = 1000000000;
        float max = 0;

        for (int i = 0; i < toNormalize.length; i++) {
            if (toNormalize[i] < min) min = toNormalize[i];
            if (toNormalize[i] > max) max = toNormalize[i];
        }

        float difference = max - min;

        float[] normalizedArray = new float[toNormalize.length];
        for (int i = 0; i < toNormalize.length; i++) {
            normalizedArray[i] = ((toNormalize[i] - min) / difference) * 3;
        }

        return normalizedArray;
    }

    private void makeSoundGraph(String inputString) {
        String[] eachSoundData = inputString.split("\\n");

        long[] soundCycleTimes = new long[eachSoundData.length];
        float[] soundCycle = new float[eachSoundData.length];

        for (int i = 0; i < eachSoundData.length; i++) {
            soundCycleTimes[i] = Long.parseLong(eachSoundData[i].split("-")[0]);
            soundCycle[i] = Float.parseFloat(eachSoundData[i].split("-")[2]);
        }

        soundCycle = normalize(soundCycle);

        GraphView.GraphViewData[] graphViewData = new GraphView.GraphViewData[eachSoundData.length];

        for (int l = 0; l < graphViewData.length; l++) {
            graphViewData[l] = new GraphView.GraphViewData(soundCycleTimes[l], soundCycle[l]);
        }

        // init example series data
        GraphViewSeries exampleSeries = new GraphViewSeries(graphViewData);

        GraphView graphView = new LineGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView.setManualYAxisBounds(4, 0);
//        graphView.getGraphViewStyle().setNumHorizontalLabels(3);
//        graphView.setHorizontalLabels(new String[] {""});
        graphView.setHorizontalLabels(new String[] {"", "", "", ""});
        graphView.getGraphViewStyle().setGridColor(Color.argb(0, 0, 0, 0));
        graphView.addSeries(exampleSeries); // data

        LinearLayout gl = (LinearLayout) findViewById(R.id.graphLayout);
        gl.addView(graphView);

    }

    private void makeSleepGraph(String inputString) {
        String[] eachSleepType = inputString.split("\\n");

        long[] sleepCycleTimes = new long[eachSleepType.length+1];
        int[] sleepCycles = new int[eachSleepType.length+1];

        for (int i = 0; i < eachSleepType.length; i++) {
            sleepCycleTimes[i] = Long.parseLong(eachSleepType[i].split("-")[0]);
            sleepCycles[i] = Integer.parseInt(eachSleepType[i].split("-")[2]);

            if (i == eachSleepType.length-1) {
                sleepCycleTimes[i+1] = Long.parseLong(eachSleepType[i].split("-")[1]);
                sleepCycles[i+1] = sleepCycles[i];

            }
        }
        Log.d("response", sleepCycleTimes[0] + " " + sleepCycleTimes[sleepCycleTimes.length-1]);

        int numberOfBucket = (int) ((sleepCycleTimes[sleepCycleTimes.length-1] - sleepCycleTimes[0]) / 30000.0f);

        float[] timeStamps = new float[numberOfBucket];
        int[] cycleStamps = new int[numberOfBucket];

        for (int j = 0; j < numberOfBucket; j++) {
            timeStamps[j] = sleepCycleTimes[0] + (30000 * j);
            if (timeStamps[j] >= sleepCycleTimes[sleepCycleTimes.length-1]) {
                cycleStamps[j] = sleepCycles[sleepCycles.length-1];
            }
            else
            {
                for (int k = 0; k < sleepCycleTimes.length; k++) {
                    if (timeStamps[j] >= sleepCycleTimes[k] && timeStamps[j] < sleepCycleTimes[k+1]) {
                        cycleStamps[j] = sleepCycles[k];
                    }
                }
            }
        }

        GraphView.GraphViewData[] graphViewData = new GraphView.GraphViewData[timeStamps.length];

        for (int l = 0; l < graphViewData.length; l++) {
            graphViewData[l] = new GraphView.GraphViewData(timeStamps[l], cycleStamps[l]);
        }

        for (int m = 0; m < timeStamps.length; m++) {
            Log.d("checkarray", timeStamps[m] + "");
        }


        // init example series data
        GraphViewSeries exampleSeries = new GraphViewSeries(graphViewData);

        GraphView graphView = new BarGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView.setManualYAxisBounds(4, 0);
//        graphView.getGraphViewStyle().setNumHorizontalLabels(3);
//        graphView.setHorizontalLabels(new String[] {""});
        graphView.setHorizontalLabels(new String[] {"", "", "", ""});
        graphView.getGraphViewStyle().setGridColor(Color.argb(0, 0, 0, 0));
        graphView.addSeries(exampleSeries); // data

        LinearLayout gl = (LinearLayout) findViewById(R.id.graphLayout);
        gl.addView(graphView);
    }

    private String parseEnv(String original) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyhhmmss");

        String newStart = sdf.parse(original.substring(3, 17)).getTime() + "";
        String newLight = original.substring(original.indexOf("Light")+7, original.indexOf(", Sound"));
        String newSound = original.substring(original.indexOf("Sound") + 7);

        return (newStart + "-" + newLight + "-" + newSound);
    }

    private String parseMisfit(String original) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

        String newStart = sdf.parse(original.substring(0, 14)).getTime() + "";
        String newEnd = sdf.parse(original.substring(15, 29)).getTime() + "";
        String sleepType = original.substring(original.length()-1);


        return (newStart + "-" + newEnd + "-" + sleepType);

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
}
