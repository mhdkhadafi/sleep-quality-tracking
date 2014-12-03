package com.apps.muhammadkhadafi.sleepqualitytracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
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
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
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
                    if (files[i].getName().substring(0, 3).equals("env")) {
                        if (files[i].length() > 0) {
                            if (fileToOpen.getName().equals("")) fileToOpen = files[i];
                            if (fileToOpen.lastModified() < files[i].lastModified()) {
                                fileToOpen = files[i];
                            }
                        }
                    }
                }
            }
            else if (testType.equals("moves")) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
                Date d = new Date(System.currentTimeMillis());
//                fileToOpen = new File("no new move file, get first");

                for (int i = 0; i < files.length; i++) {
                    Log.d("response", files[i].getName());
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
                    Log.d("response", files[i].getName());
                    if (files[i].getName().substring(0, 6).equals("misfit")) {
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
                    aBuffer += aDataRow + "\n";
                }
                myReader.close();
                dataView.setText(aBuffer);
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
