package com.apps.muhammadkhadafi.sleepqualitytracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Created by muhammadkhadafi on 12/5/14.
 */
public class SleepCalendarActivity extends Activity {
    Button btnBack;
    Button btnGetSleep;
    int calMonth;
    int calDay;
    int calYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_calendar);

        btnBack = (Button) findViewById(R.id.btn_back);
        btnGetSleep = (Button) findViewById(R.id.btn_getsleep);
        final CalendarView calendarView = (CalendarView) findViewById(R.id.cal_sleepcalendar);

        Date d = new Date(System.currentTimeMillis());
        SimpleDateFormat sdfm = new SimpleDateFormat("MM");
        SimpleDateFormat sdfd = new SimpleDateFormat("dd");
        SimpleDateFormat sdfy = new SimpleDateFormat("yyyy");
        calDay = Integer.parseInt(sdfd.format(d));
        calMonth = Integer.parseInt(sdfm.format(d)) - 1;
        calYear = Integer.parseInt(sdfy.format(d));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
//                Toast.makeText(getApplicationContext(), "" + dayOfMonth, Toast.LENGTH_SHORT).show();// TODO Auto-generated method stub
                calDay = dayOfMonth;
                calMonth = month;
                calYear = year;
            }
        });



        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        btnGetSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SleepGraphActivity.class);
                Bundle b = new Bundle();
                b.putInt("day", calDay); //Your id
                b.putInt("month", calMonth);
                b.putInt("year", calYear);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
            }
        });



    }
}