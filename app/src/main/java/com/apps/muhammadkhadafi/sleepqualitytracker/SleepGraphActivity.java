package com.apps.muhammadkhadafi.sleepqualitytracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;

/**
 * Created by muhammadkhadafi on 12/9/14.
 */
public class SleepGraphActivity extends Activity {

    int day;
    int month;
    int year;
    String stringToGraphSleep;
    String stringToGraphEnv;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_graph);

        Bundle b = getIntent().getExtras();
        day = b.getInt("day");
        month = b.getInt("month");
        year = b.getInt("year");

        readSleepFile();

        btnBack = (Button) findViewById(R.id.btn_back);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SleepCalendarActivity.class);
                startActivity(intent);
            }
        });

    }

    private String toDoubleDigit(int digits) {
        if (digits < 10) return ("0" + digits);
        else return (digits + "");
    }

    private String parseMisfit(String original) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

        String newStart = sdf.parse(original.substring(0, 14)).getTime() + "";
        String newEnd = sdf.parse(original.substring(15, 29)).getTime() + "";
        String sleepType = original.substring(original.length()-1);


        return (newStart + "-" + newEnd + "-" + sleepType);

    }

    private void makeSleepGraph(String inputString) {
        String[] eachSleepType = inputString.split("\\n");

        long[] sleepCycleTimes = new long[eachSleepType.length+1];
        int[] sleepCycles = new int[eachSleepType.length+1];

        for (int i = 0; i < eachSleepType.length; i++) {
            sleepCycleTimes[i] = Long.parseLong(eachSleepType[i].split("-")[0]);
            sleepCycles[i] = Integer.parseInt(eachSleepType[i].split("-")[2]);

            if (sleepCycleTimes[i] < sleepCycleTimes[0]) sleepCycleTimes[i] += (1000*60*60*12);

            if (i == eachSleepType.length-1) {
                sleepCycleTimes[i+1] = Long.parseLong(eachSleepType[i].split("-")[1]);
                sleepCycles[i+1] = sleepCycles[i];

                if (sleepCycleTimes[i+1] < sleepCycleTimes[0]) sleepCycleTimes[i+1] += (1000*60*60*12);
            }
        }
        SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMddhhmmss");
        Log.d("response", sleepCycleTimes[0] + " " + sleepCycleTimes[sleepCycleTimes.length-1]);
        Log.d("response", format2.format(new Date(sleepCycleTimes[0])) + " " + format2.format(new Date(sleepCycleTimes[sleepCycleTimes.length-1])));

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

        SimpleDateFormat sdfdate = new SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat sdftime = new SimpleDateFormat("hh:mm:ss");

        // init example series data
        GraphViewSeries exampleSeries = new GraphViewSeries(graphViewData);

        GraphView graphView = new BarGraphView(
                this // context
                , "Sleep Quality Graph (" + sdfdate.format(new Date(sleepCycleTimes[0])) + ")" // heading
        );
        graphView.setManualYAxisBounds(4, 0);
//        graphView.getGraphViewStyle().setNumHorizontalLabels(3);
//        graphView.setHorizontalLabels(new String[] {""});
        graphView.setVerticalLabels(new String[]{"", "3", "2", "1", "0"});
        graphView.setHorizontalLabels(new String[]{sdftime.format(new Date(sleepCycleTimes[0])), "-",
                sdftime.format(new Date(sleepCycleTimes[sleepCycleTimes.length-1]))});
        graphView.getGraphViewStyle().setGridColor(Color.argb(0, 0, 0, 0));
        graphView.addSeries(exampleSeries); // data

        LinearLayout gl = (LinearLayout) findViewById(R.id.layout_graphsleep);
        gl.addView(graphView);
    }

    private void readSleepFile() {
        try {
            File fileToOpen = new File("file not found");
            File[] files = getExternalFilesDir("MyFileStorage").listFiles();

//            Log.d("response", "in misfit" + files.length);
//            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
//            Date d = new Date(System.currentTimeMillis());
//                fileToOpen = new File("no new move file, get first");

            for (int i = 0; i < files.length; i++) {
//                Log.d("filetoopen", "misfit" + year + toDoubleDigit(month + 1) + toDoubleDigit(day));

                if (files[i].getName().equals("misfit" + year + toDoubleDigit(month + 1) + toDoubleDigit(day) + ".txt")) {
                    fileToOpen = files[i];
                    break;
                }
            }

            Log.d("filetoopen", "misfit" + fileToOpen.getName());


            if (fileToOpen.exists()) {
                FileInputStream fIn = new FileInputStream(fileToOpen);
                BufferedReader myReader = new BufferedReader(
                        new InputStreamReader(fIn));
                String aDataRow = "";
                String aBuffer = "";
                boolean firstRow = true;
                while ((aDataRow = myReader.readLine()) != null) {
                    if (firstRow) {readEnvFile(aDataRow); firstRow = false;}
                    aBuffer += parseMisfit(aDataRow) + "\n";
                }
                myReader.close();

//                dataView.setText(aBuffer);
                stringToGraphSleep = aBuffer;
//                if (testType.equals("misfit")) {
//                    makeGraph(aBuffer);
//                }
                TextView txtDebug = (TextView) findViewById(R.id.txt_debug);
                txtDebug.setText("");
                makeSleepGraph(stringToGraphSleep);
            }
            else {
//                dataView.setText("no files yet, get first?");
            }
        }
        catch (Exception e) {
//            Toast.makeText(getBaseContext(), e.getMessage(),
//                    Toast.LENGTH_SHORT).show();
//            dataView.setText("no files yet, get first?");
        }

    }

    private String parseEnv(String original) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyhhmmss");

        String newStart = sdf.parse(original.substring(3, 17)).getTime() + "";
        String newLight = original.substring(original.indexOf("Light")+7, original.indexOf(", Sound"));
        String newSound = original.substring(original.indexOf("Sound") + 7);

        return (newStart + "-" + newLight + "-" + newSound);
    }

    private float[] normalizeSound(float[] toNormalize) {
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

    private float[] normalizeLight(float[] toNormalize) {
        float min = 1000000000;
        float max = 0;

        for (int i = 0; i < toNormalize.length; i++) {
            if (toNormalize[i] < min) min = toNormalize[i];
            if (toNormalize[i] > max) max = toNormalize[i];
        }

        float offset = 0.1f;
        float difference = max - min;
        if (difference == 0) {
            difference = 1;
            if (min == 0 && max == 0) {
                offset = 0.1f;
            }
            else
            {
                offset = 1.1f;
            }
        }



        float[] normalizedArray = new float[toNormalize.length];
        for (int i = 0; i < toNormalize.length; i++) {
            normalizedArray[i] = (((toNormalize[i] - min) / difference) * 3) + offset;
        }

        return normalizedArray;
    }

    private void makeLightGraph(String inputString) {
        String[] eachLightData = inputString.split("\\n");

        long[] lightCycleTimes = new long[eachLightData.length];
        float[] lightCycle = new float[eachLightData.length];

        for (int i = 0; i < eachLightData.length; i++) {
            lightCycleTimes[i] = Long.parseLong(eachLightData[i].split("-")[0]);
            lightCycle[i] = Float.parseFloat(eachLightData[i].split("-")[1]);

            Log.d("lightc", lightCycle[i]+ "");
            if ((i >= 0 && i <= 6) || (i >= eachLightData.length - 6 && i < eachLightData.length)) {
                lightCycle[i] = Float.parseFloat(eachLightData[eachLightData.length/2].split("-")[1]);
            }
            Log.d("lightc", lightCycle[i]+ "");
        }

        lightCycle = normalizeLight(lightCycle);

        GraphView.GraphViewData[] graphViewData = new GraphView.GraphViewData[eachLightData.length];

        for (int l = 0; l < graphViewData.length; l++) {
            graphViewData[l] = new GraphView.GraphViewData(lightCycleTimes[l], lightCycle[l]);
        }

        // init example series data
        GraphViewSeries exampleSeries = new GraphViewSeries("", new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 3), graphViewData);

        GraphView graphView = new LineGraphView(
                this // context
                , "" // heading
        );
        graphView.setManualYAxisBounds(4, 0);
//        graphView.getGraphViewStyle().setNumHorizontalLabels(3);
//        graphView.setHorizontalLabels(new String[] {""});
        graphView.setVerticalLabels(new String[]{""});
        graphView.setHorizontalLabels(new String[]{""});
        graphView.getGraphViewStyle().setGridColor(Color.argb(0, 0, 0, 0));
        graphView.addSeries(exampleSeries); // data

        LinearLayout gl = (LinearLayout) findViewById(R.id.layout_graphlight);
        gl.addView(graphView);

    }

    private void makeSoundGraph(String inputString) {
        String[] eachSoundData = inputString.split("\\n");

        long[] soundCycleTimes = new long[eachSoundData.length];
        float[] soundCycle = new float[eachSoundData.length];

        for (int i = 0; i < eachSoundData.length; i++) {
            soundCycleTimes[i] = Long.parseLong(eachSoundData[i].split("-")[0]);
            soundCycle[i] = Float.parseFloat(eachSoundData[i].split("-")[2]);

            if ((i >= 1 && i <= 6) || (i >= eachSoundData.length - 6 && i < eachSoundData.length)) {
                soundCycle[i] = Float.parseFloat(eachSoundData[eachSoundData.length/2].split("-")[2]);
            }
        }

        soundCycle = normalizeSound(soundCycle);

        GraphView.GraphViewData[] graphViewData = new GraphView.GraphViewData[eachSoundData.length];

        for (int l = 0; l < graphViewData.length; l++) {
            graphViewData[l] = new GraphView.GraphViewData(soundCycleTimes[l], soundCycle[l]);
        }

        // init example series data
        GraphViewSeries exampleSeries = new GraphViewSeries("", new GraphViewSeries.GraphViewSeriesStyle(Color.BLACK, 3), graphViewData);

        GraphView graphView = new LineGraphView(
                this // context
                , "" // heading
        );
        graphView.setManualYAxisBounds(4, 0);
//        graphView.getGraphViewStyle().setNumHorizontalLabels(3);
//        graphView.setHorizontalLabels(new String[] {""});
        graphView.setVerticalLabels(new String[] {""});
        graphView.setHorizontalLabels(new String[] {""});
        graphView.getGraphViewStyle().setGridColor(Color.argb(0, 0, 0, 0));
        graphView.addSeries(exampleSeries); // data

        LinearLayout gl = (LinearLayout) findViewById(R.id.layout_graphsound);
        gl.addView(graphView);

    }

    private void readEnvFile(String sleepFirstRow) throws ParseException {
        String sleepTime = sleepFirstRow.split("-")[0];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MMddyyyyhhmmss");

        Date sleepDate = sdf.parse(sleepTime);

        try {
            File fileToOpen = new File("file not found");
            File[] files = getExternalFilesDir("MyFileStorage").listFiles();

            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().substring(0, 3).equals("env")) {
                    if (sdf2.parse(files[i].getName().substring(3)).getTime() < sleepDate.getTime()) {
                        if (fileToOpen.getName().equals("file not found")) {
                            fileToOpen = files[i];
                        }
                        else {
                            if (sdf2.parse(files[i].getName().substring(3)).getTime() > sdf2.parse(fileToOpen.getName().substring(3)).getTime()) {
                                fileToOpen = files[i];
                            }
                        }
                    }
                }
            }

            Log.d("filetoopen", "env" + fileToOpen.getName());


            if (fileToOpen.exists()) {
                FileInputStream fIn = new FileInputStream(fileToOpen);
                BufferedReader myReader = new BufferedReader(
                        new InputStreamReader(fIn));
                String aDataRow = "";
                String aBuffer = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    aBuffer += parseEnv(aDataRow) + "\n";
                }
                myReader.close();

//                dataView.setText(aBuffer);
                stringToGraphEnv = aBuffer;
//                if (testType.equals("misfit")) {
//                    makeGraph(aBuffer);
//                }
                makeLightGraph(stringToGraphEnv);
                makeSoundGraph(stringToGraphEnv);
            }
            else {
//                dataView.setText("no files yet, get first?");
            }

        }
        catch (Exception e) {
//            Toast.makeText(getBaseContext(), e.getMessage(),
//                    Toast.LENGTH_SHORT).show();
//            dataView.setText("no files yet, get first?");
        }
    }
}
