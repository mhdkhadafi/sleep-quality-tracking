package com.apps.muhammadkhadafi.sleepqualitytracker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

/**
 * Created by muhammadkhadafi on 12/9/14.
 */
public class GraphActivity extends Activity{

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // init example series data
        GraphViewSeries exampleSeries = new GraphViewSeries(new GraphView.GraphViewData[] {
                new GraphView.GraphViewData(1, 2.0d)
                , new GraphView.GraphViewData(2, 1.5d)
                , new GraphView.GraphViewData(3, 2.5d)
                , new GraphView.GraphViewData(4, 1.0d)
        });

        GraphView graphView = new BarGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView.setManualYAxisBounds(3, 0);
//        graphView.getGraphViewStyle().setNumHorizontalLabels(3);
        graphView.setHorizontalLabels(new String[] {""});
        graphView.getGraphViewStyle().setGridColor(Color.argb(0, 0, 0, 0));
        graphView.addSeries(exampleSeries); // data

        // init example series data
        GraphViewSeries exampleSeries2 = new GraphViewSeries("", new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 10), new GraphView.GraphViewData[] {
                new GraphView.GraphViewData(0, 2.0d)
                , new GraphView.GraphViewData(0.5, 2.0d)
                , new GraphView.GraphViewData(1.5, 1.5d)
                , new GraphView.GraphViewData(2.5, 2.5d)
                , new GraphView.GraphViewData(3.5, 1.0d)
                , new GraphView.GraphViewData(4, 1.0d)
        });

        GraphView graphView2 = new LineGraphView(
                this // context
                , "GraphViewDemo" // heading
        );
        graphView2.setManualYAxisBounds(3, 0);
//        graphView2.getGraphViewStyle().setNumHorizontalLabels(1);
        graphView2.addSeries(exampleSeries2); // data
        graphView2.setHorizontalLabels(new String[] {"", "1", "", "2", "", "3", "", "4", ""});

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout1);
        layout.addView(graphView);

        LinearLayout layout2 = (LinearLayout) findViewById(R.id.layout2);
        layout2.addView(graphView2);

    }
}
