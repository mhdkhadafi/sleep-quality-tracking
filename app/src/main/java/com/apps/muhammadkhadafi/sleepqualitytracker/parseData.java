package com.apps.muhammadkhadafi.sleepqualitytracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by oza on 12/3/14.
 */
public class ParseData {

    private static final int TIME = 1;
    private static final int LIGHT = 2;
    private static final int SOUND = 3;
    private static ArrayList<Date> timeArrayList = new ArrayList<Date>();
    private static ArrayList<Float> lightArrayList = new ArrayList<Float>();
    private static ArrayList<Float> soundArrayList = new ArrayList<Float>();
    private static Object[] sleepList = {timeArrayList.toArray(), lightArrayList.toArray(), soundArrayList.toArray()};

    private static Object[] parseData(File sleepDir, int arg) {

        SimpleDateFormat sdf = new SimpleDateFormat("MMDDYYYYHHmmss");
        File[] sleepFiles = sleepDir.listFiles();

        try {
            for (File file : sleepFiles) {
                BufferedReader parse = new BufferedReader(new FileReader(file.getAbsoluteFile()));
                String line;
                while ((line = parse.readLine()) != null) {
                    if (line.contains("log")) {
                        timeArrayList.add(sdf.parse(line.split("log")[1].split("----")[0]));
                        lightArrayList.add(Float.parseFloat(line.split("log")[1].split("----")[1].split("Light: ")[1].split(", ")[0]));
                        soundArrayList.add(Float.parseFloat(line.split("log")[1].split("----")[1].split("Sound: ")[1].split(" ")[0]));
                    }
                }
                parse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (arg == TIME)
            return timeArrayList.toArray();
        if (arg == SOUND)
            return soundArrayList.toArray();
        if (arg == LIGHT)
            return lightArrayList.toArray();
        return sleepList;
    }

    private static Date[] getTimeArray() {
        Date[] timeArray = new Date[Array.getLength(timeArrayList.toArray())];
        for (int i = 0; i < timeArray.length; i++)
            timeArray[i] = (Date) Array.get(timeArrayList.toArray(), i);
        return timeArray;
    }

    private static Date[] getLightArray() {
        Date[] lightArray = new Date[Array.getLength(lightArrayList.toArray())];
        for (int i = 0; i < lightArray.length; i++)
            lightArray[i] = (Date) Array.get(lightArrayList.toArray(), i);
        return lightArray;
    }

    private static Date[] getSoundArray() {
        Date[] soundArray = new Date[Array.getLength(soundArrayList.toArray())];
        for (int i = 0; i < soundArray.length; i++)
            soundArray[i] = (Date) Array.get(soundArrayList.toArray(), i);
        return soundArray;
    }

    public static void main(String[] args) {
        Object[] parsed = parseData(new File("/Users/oza/Documents/MyFileStorage"), 0);
//        for (int i = 0; i < Array.getLength(Array.get(parsed, 0)); i++) {
//            System.out.println(Array.get(Array.get(parsed, 0), i) + " " + Array.get(Array.get(parsed, 1), i) + " " + Array.get(Array.get(parsed, 2), i));
//        }
        for (int i = 0; i < getTimeArray().length; i++)
        System.out.println(Array.get(getTimeArray(),i).toString());
    }
}


